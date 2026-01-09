# API Security Spring Boot Starter

## 简介

`api-security-spring-boot-starter` 是一个轻量级 Spring Boot Starter，为 API 接口提供无侵入的安全增强能力，包括请求解密、响应加密、签名验签、防重复提交与超时校验等。通过注解与配置即可快速集成，同时支持多算法扩展。

## 核心功能

- 请求解密/响应加密（AES/RSA）
- 请求验签/响应签名（MD5/RSA）
- 防重复提交与超时校验
- 签名参数 SpEL 表达式
- 算法可插拔扩展（注册 Bean 即可）

## 快速集成

### 环境要求

- JDK 1.8+
- Spring Boot 2.7.x

### 引入依赖

```xml
<dependency>
    <groupId>cn.coderxiaoc</groupId>
    <artifactId>api-security-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 最小配置示例

> Starter 自动生效，无需额外 `@Enable*` 注解。

```yaml
web-security:
  cipher:
    default-algorithm: aes
    aes:
      secret-key: "your-aes-secret"
  signature:
    default-algorithm: rsa
    rsa:
      private-key: "your-rsa-private-key"
      public-key: "your-rsa-public-key"
```

## 算法选择规则

算法选择顺序如下（加密与签名一致）：

1. 注解上的 `algorithm`
2. 配置中的 `default-algorithm`
3. 当前只存在一个算法实现时自动使用

内置算法标识：

- AES 加密：`aes`
- RSA 加密：`rsa`
- MD5 签名：`md5`
- RSA 签名：`rsa`

`algorithm` 与 `secretKey` 支持占位符（如 `${app.key}`）。

## 核心注解

### 1. @Encrypt：响应数据加密

| 参数 | 类型 | 说明 | 默认值 |
| --- | --- | --- | --- |
| value/secretKey | String | 加密密钥（可用占位符） | "" |
| encryptField | String | 需要加密的字段（空则加密整个响应体） | "" |
| algorithm | String | 指定加密算法 | "" |
| merge | boolean | 是否将加密结果回写到原字段 | true |

### 2. @Decrypt：请求数据解密

| 参数 | 类型 | 说明 | 默认值 |
| --- | --- | --- | --- |
| value/secretKey | String | 解密密钥（可用占位符） | "" |
| decryptField | String | 指定要解密的字段 | "" |
| decryptResultField | String | 从解密后的结果中提取字段 | "" |
| algorithm | String | 指定解密算法 | "" |
| merge | boolean | 是否将解密结果回写到原字段 | true |

### 3. @Verification：请求签名验证

| 参数 | 类型 | 说明 | 默认值 |
| --- | --- | --- | --- |
| value | String | 签名参数表达式 | 必填 |
| runtimeType | RuntimeType | 校验时机（BEFORE/AFTER） | BEFORE |
| signatureField | String | 请求头签名字段 | 必填 |
| algorithm | String | 指定签名算法 | "" |
| preventDuplicateField | String | 防重复字段（如 nonce） | "" |
| timeoutField | String | 超时字段（如 timestamp） | "" |
| timeout | long | 超时时间 | 30 |
| timeUnit | TimeUnit | 超时单位 | SECONDS |
| delimiter | String | 签名参数拼接符 | "|" |
| splitter | String | 表达式分隔符 | "&" |

### 4. @Signature：响应签名生成

| 参数 | 类型 | 说明 | 默认值 |
| --- | --- | --- | --- |
| value | String | 签名参数表达式 | 必填 |
| runtimeType | RuntimeType | 签名时机（BEFORE/AFTER） | BEFORE |
| signatureField | String | 响应头签名字段 | 必填 |
| algorithm | String | 指定签名算法 | "" |
| delimiter | String | 签名参数拼接符 | "|" |
| splitter | String | 表达式分隔符 | "&" |

`runtimeType` 控制签名/验签是在加解密前还是后执行：

- `BEFORE`：在解密/加密之前
- `AFTER`：在解密/加密之后

## 配置项

### 加密配置

| 配置项 | 类型 | 说明 |
| --- | --- | --- |
| web-security.cipher.default-algorithm | String | 默认加密算法 |
| web-security.cipher.aes.secret-key | String | AES 密钥 |
| web-security.cipher.rsa.private-key | String | RSA 私钥字符串 |
| web-security.cipher.rsa.public-key | String | RSA 公钥字符串 |
| web-security.cipher.rsa.private-path | String | RSA 私钥文件路径（类路径） |
| web-security.cipher.rsa.public-path | String | RSA 公钥文件路径（类路径） |

### 签名配置

| 配置项 | 类型 | 说明 |
| --- | --- | --- |
| web-security.signature.default-algorithm | String | 默认签名算法 |
| web-security.signature.md5.secret-key | String | MD5 密钥 |
| web-security.signature.md5.delimiter | String | MD5 拼接符 |
| web-security.signature.rsa.private-key | String | RSA 私钥字符串 |
| web-security.signature.rsa.public-key | String | RSA 公钥字符串 |
| web-security.signature.rsa.private-path | String | RSA 私钥文件路径（类路径） |
| web-security.signature.rsa.public-path | String | RSA 公钥文件路径（类路径） |

### 防重复提交与超时

| 配置项 | 类型 | 说明 | 默认值 |
| --- | --- | --- | --- |
| web-security.signature.enable-prevent-duplicate | Boolean | 是否开启防重复 | false |
| web-security.signature.prevent-duplicate-field | String | 防重复字段名 | x-s-nonce |
| web-security.signature.prevent-duplicate-timeout | Long | 防重复超时时间 | 30 |
| web-security.signature.prevent-duplicate-time-unit | TimeUnit | 防重复时间单位 | SECONDS |
| web-security.signature.prevent-duplicate-prefix | String | Redis Key 前缀 | preventDuplicate |
| web-security.signature.enable-timeout | Boolean | 是否开启超时校验 | false |
| web-security.signature.timeout-field | String | 超时字段名 | x-s-timestamp |
| web-security.signature.timeout | Long | 超时时间 | 30 |
| web-security.signature.in-memory-clean-interval | Long | 内存去重清理周期 | 30 |
| web-security.signature.in-memory-clean-interval-time-unit | TimeUnit | 内存清理时间单位 | SECONDS |

## 签名参数表达式

### 表达式解析规则

`@Verification/@Signature` 的 `value` 使用 SpEL 表达式，多个表达式通过 `splitter` 分隔，最终通过 `delimiter` 拼接得到待签名字符串。

### 上下文变量

- `#params.header(key)`：获取请求/响应头字段
- `#params.body(key)`：获取请求/响应体字段
- `#params.bodyJson()`：获取请求/响应体 JSON
- `#sing.getNonce(field)` / `#sing.getNonce(field, len)`：生成随机串并写入响应头
- `#sing.getTimestamp(field)`：生成时间戳并写入响应头
- `#request` / `#response`：原始请求/响应对象（仅响应签名场景）

### 自定义 Bean 调用

可在 SpEL 中调用自定义 Bean 方法：

```java
@Component("mySignUtil")
public class MySignUtil {
    public String buildParams(SignatureParams params) {
        return params.header("x-token") + "_" + params.body("userId");
    }
}
```

```java
@Signature(
    value = "@mySignUtil.buildParams(#params)&@mySignUtil.buildParams(#params)",
    signatureField = "x-s-sign"
)
```

## 自定义算法扩展

### 加密算法扩展

实现 `Cipher` 并返回唯一 `algorithm()`，注册为 Spring Bean：

```java
@Component
public class Sm4Cipher implements Cipher {
    @Override
    public String algorithm() {
        return "sm4";
    }
    // encrypt/decrypt 实现
}
```

然后通过注解或默认配置指定 `algorithm = "sm4"`。

### 签名算法扩展

实现 `Signature` 并返回唯一 `algorithm()`，注册为 Spring Bean：

```java
@Component
public class Sm2Signature implements Signature {
    @Override
    public String algorithm() {
        return "sm2";
    }
    // sign/verify 实现
}
```

## 完整使用示例

```java
@RestController
@RequestMapping("/api/v1/trade")
public class TradeController {

    @PostMapping("/submit")
    @Decrypt(
        secretKey = "${app.decrypt.key}",
        decryptField = "data",
        decryptResultField = "data",
        algorithm = "aes"
    )
    @Encrypt(
        secretKey = "${app.encrypt.key}",
        encryptField = "data",
        algorithm = "aes"
    )
    @Verification(
        runtimeType = RuntimeType.BEFORE,
        value = "#params.header('x-token')&#params.header('x-nonce')&#params.header('x-timestamp')&#params.body('data')",
        signatureField = "x-s-sign",
        preventDuplicateField = "x-nonce",
        timeoutField = "x-timestamp",
        timeout = 60,
        algorithm = "rsa"
    )
    @Signature(
        runtimeType = RuntimeType.AFTER,
        value = "#params.header('x-token')&#sing.getNonce('x-nonce')&#sing.getTimestamp('x-timestamp')&#params.body('data')",
        signatureField = "x-s-sign",
        algorithm = "rsa"
    )
    public Result<TradeResponse> submitTrade(@RequestBody TradeRequest request) {
        return Result.success(new TradeResponse());
    }
}
```

## 升级提示（不兼容变更）

- `Cipher` 与 `Signature` 接口新增 `algorithm()`，已有实现需补齐。
- 注解新增 `algorithm` 参数，默认算法从配置中读取。
- `@EnableCipher/@EnableSignature` 不再负责注册配置类，Starter 直接生效。

## 项目地址

- GitHub：https://github.com/codermyxiaoc/api-security-spring-boot-starter
