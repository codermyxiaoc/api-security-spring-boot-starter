## 简介

`api-security-spring-boot-starter` 是一个轻量级的 Spring Boot Starter，专注于为 API 接口提供无代码侵入的安全增强能力，包括自动数据加密解密、签名验证、防重复提交及超时控制等功能。通过注解式配置，开发者可快速集成安全防护机制，有效抵御数据泄露、请求篡改、重放攻击等常见安全风险。

## 核心功能

- **数据加密解密**：支持自动请求数据解密与响应数据加密，保护敏感信息传输
- **签名验证**：支持 MD5、RSA 两种签名算法，验证请求合法性
- **防重复提交**：基于内存或 Redis 实现请求去重，防止重复提交
- **超时控制**：验证请求时间戳，拒绝超时请求
- **响应签名**：自动为响应生成签名，供客户端验证响应完整性

## 快速集成

### 环境要求

- JDK 1.8+
- Spring Boot 2.7.x

### 引入依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cn.coderxiaoc</groupId>
    <artifactId>api-security-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 核心注解详解

### 1. @Encrypt：响应数据加密

用于对接口返回的响应数据进行加密处理，可标注在类或方法上（方法级优先级更高）。

#### 注解参数

| 参数名         | 类型    | 说明                                       | 默认值   |
| -------------- | ------- | ------------------------------------------ | -------- |
| `value`        | String  | 加密密钥（与`secretKey`互为别名）          | 空字符串 |
| `secretKey`    | String  | 加密密钥（与`value`互为别名）              | 空字符串 |
| `encryptField` | String  | 指定需要加密的字段（为空则加密整个响应体） | 空字符串 |
| `merge`        | boolean | 加密后是否合并回原响应体（替换原字段）     | true     |

### 2. @Decrypt：请求数据解密

用于对接口接收的请求数据进行解密处理，可标注在类或方法上（方法级优先级更高）。

#### 注解参数

| 参数名               | 类型    | 说明                                         | 默认值   |
| -------------------- | ------- | -------------------------------------------- | -------- |
| `value`              | String  | 解密密钥（与`secretKey`互为别名）            | 空字符串 |
| `secretKey`          | String  | 解密密钥（与`value`互为别名）                | 空字符串 |
| `decryptField`       | String  | 指定需要解密的字段（为空则解密整个请求体）   | 空字符串 |
| `decryptResultField` | String  | 解密后从结果中提取的目标字段（用于嵌套结构） | 空字符串 |
| `merge`              | boolean | 解密后是否合并回原请求体（替换原字段）       | true     |

### 3. @Verification：请求签名验证

用于验证请求签名合法性，支持防重复提交和超时控制，仅标注在方法上。

#### 注解参数

| 参数名                  | 类型        | 说明                                 | 默认值      |      |
| ----------------------- | ----------- | ------------------------------------ | ----------- | ---- |
| `value`                 | String      | 签名参数表达式（生成待签名字符串）   | 无（必填）  |      |
| `runtimeType`           | RuntimeType | 验证时机（`BEFORE`/`AFTER`请求解析） | BEFORE      |      |
| `signatureField`        | String      | 请求头中存储签名的字段名             | 无（必填）  |      |
| `preventDuplicateField` | String      | 防重复提交的标识字段（如 nonce）     | 空字符串    |      |
| `timeoutField`          | String      | 超时验证的时间戳字段名               | 空字符串    |      |
| `timeout`               | long        | 超时时间（结合`timeoutField`）       | 0（不启用） |      |
| `delimiter`             | String      | 签名参数拼接分隔符                   | "           | "    |
| `splitter`              | String      | 表达式中参数的分隔符                 | "&"         |      |

### 4. @Signature：响应签名生成

用于为接口响应生成签名并添加到响应头，可标注在类或方法上。

#### 注解参数

| 参数名           | 类型        | 说明                                     | 默认值     |      |
| ---------------- | ----------- | ---------------------------------------- | ---------- | ---- |
| `value`          | String      | 签名参数表达式（生成待签名字符串）       | 无（必填） |      |
| `runtimeType`    | RuntimeType | 签名生成时机（`BEFORE`/`AFTER`响应处理） | BEFORE     |      |
| `signatureField` | String      | 响应头中存储签名的字段名                 | 无（必填） |      |
| `delimiter`      | String      | 签名参数拼接分隔符                       | "          | "    |
| `splitter`       | String      | 表达式中参数的分隔符                     | "&"        |      |

## 签名算法配置

支持 **MD5** 和 **RSA** 两种签名算法，可通过配置指定使用的算法。

### 1. MD5 签名配置

#### 配置参数（前缀：`web-security.signature.md5`）

| 参数名      | 类型   | 说明                 | 默认值 |      |
| ----------- | ------ | -------------------- | ------ | ---- |
| `secretKey` | String | MD5 签名密钥（必填） | 无     |      |
| `delimiter` | String | 签名参数拼接分隔符   | \|     |      |

#### 配置示例

```yaml
web-security:
  signature:
    md5:
      secret-key: "your-md5-secret-key"
      delimiter: "|"
```

### 2. RSA 签名配置

#### 配置参数（前缀：`web-security.signature.rsa`）

| 参数名        | 类型   | 说明                                    | 默认值 |
| ------------- | ------ | --------------------------------------- | ------ |
| `privateKey`  | String | RSA 私钥字符串（与`privatePath`二选一） | 无     |
| `publicKey`   | String | RSA 公钥字符串（与`publicPath`二选一）  | 无     |
| `privatePath` | String | RSA 私钥文件路径（类路径下）            | 无     |
| `publicPath`  | String | RSA 公钥文件路径（类路径下）            | 无     |

#### 配置示例（使用文件路径）

```yaml
web-security:
  signature:
    rsa:
      private-path: "rsa/private.pem"  # 类路径下的私钥文件
      public-path: "rsa/public.pem"    # 类路径下的公钥文件
```

#### 生成 RSA 密钥对

可使用工具类 `RSAUtil` 生成密钥对：

```java
// 生成密钥对字符串
Map<String, String> keyPair = RSAUtil.createKeyPairWithString();
String publicKey = keyPair.get("publicKey");
String privateKey = keyPair.get("privateKey");

// 生成密钥对文件
RSAUtil.createKeyPairWithFile("public.pem", "private.pem");
```

## 防重复提交配置

支持 **内存存储** 和 **Redis 存储** 两种方式，默认使用内存存储。

### 配置参数（前缀：`web-security.signature`）

| 参数名                     | 类型     | 说明                             | 默认值             |
| -------------------------- | -------- | -------------------------------- | ------------------ |
| `enablePreventDuplicate`   | Boolean  | 是否启用防重复提交               | false              |
| `preventDuplicateField`    | String   | 防重复标识字段名（如 x-s-nonce） | "x-s-nonce"        |
| `preventDuplicateTimeout`  | Long     | 防重复过期时间                   | 5                  |
| `preventDuplicateTimeUnit` | TimeUnit | 防重复时间单位                   | SECONDS            |
| `preventDuplicatePrefix`   | String   | 防重复缓存键前缀                 | "preventDuplicate" |
| `enableTimeout`            | Boolean  | 是否启用超时验证                 | false              |
| `timeoutField`             | String   | 时间戳字段名                     | "x-s-timestamp"    |
| `timeout`                  | Long     | 超时时间（秒）                   | 5                  |

### 配置示例（启用 Redis）

需额外引入 Redis 依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
web-security:
  signature:
    enable-prevent-duplicate: true
    prevent-duplicate-timeout: 60  # 防重复有效期60秒
    enable-timeout: true
    timeout: 300  # 超时时间300秒（5分钟）
```

## 签名参数表达式

`@Verification` 和 `@Signature` 的 `value` 参数支持 SpEL 表达式，用于提取签名所需参数，支持以下变量：

- `#params.header(key)`：获取请求头参数（如`#params.header('x-s-token')`）
- `#params.body(key)`：获取请求体 / 响应体字段（如`#params.body('data')`）
- `#sing.getNonce(field)`：生成随机串并添加到响应头（如`#sing.getNonce('x-s-nonce')`）
- `#sing.getNonce(field, len)`：生成指定长度的随机串（如`#sing.getNonce('x-s-nonce', 10)`）
- `#sing.getTimestamp(field)`：生成当前时间戳并添加到响应头（如`#sing.getTimestamp('x-s-timestamp')`）

## 异常处理

| 异常类                           | 说明             | 解决方案                            |
| -------------------------------- | ---------------- | ----------------------------------- |
| `MD5Exception`                   | MD5 签名过程异常 | 检查 MD5 密钥是否配置，参数是否合法 |
| `ReadSecretKeyException`         | RSA 密钥读取异常 | 检查密钥文件路径或密钥字符串格式    |
| `SignatureParamsEmptyException`  | 签名参数解析为空 | 检查`value`表达式是否正确提取参数   |
| `InvalidSignatureFieldException` | 签名字段未配置   | 确保`signatureField`参数已设置      |

## 完整使用示例

```java
@RestController
@RequestMapping("/api/v1/trade")
public class TradeController {

    // 完整安全配置：解密请求 + 验证签名 + 加密响应 + 生成响应签名
    @PostMapping("/submit")
    @Decrypt(
        secretKey = "${app.decrypt.key}",
        decryptField = "data",
        decryptResultField = "data"
    )
    @Encrypt(
        secretKey = "${app.encrypt.key}",
        encryptField = "data"
    )
    @Verification(
        runtimeType = RuntimeType.BEFORE,
        value = "#params.header('x-s-token')&#params.header('x-s-nonce')&#params.header('x-s-timestamp')&#params.body('data')",
        signatureField = "x-s-sing",
        preventDuplicateField = "x-s-nonce",
        timeoutField = "x-s-timestamp",
        timeout = 60
    )
    @Signature(
        runtimeType = RuntimeType.AFTER,
        value = "#params.header('x-s-token')&#sing.getNonce('x-s-nonce')&#sing.getTimestamp('x-s-timestamp')&#params.body('data')",
        signatureField = "x-s-sing"
    )
    public Result<TradeResponse> submitTrade(@RequestBody TradeRequest request) {
        // 业务逻辑处理
        return Result.success(new TradeResponse(...));
    }
}
```

## 项目地址

- GitHub：https://github.com/codermyxiaoc/api-security-spring-boot-starter
- 许可证：MIT License# API Security Spring Boot Starter 文档

## 简介

`api-security-spring-boot-starter` 是一个轻量级的 Spring Boot Starter，专注于为 API 接口提供安全增强能力，包括数据加密解密、签名验证、防重复提交及超时控制等功能。通过注解式配置，开发者可快速集成安全防护机制，有效抵御数据泄露、请求篡改、重放攻击等常见安全风险。

## 核心功能

- **数据加密解密**：支持请求数据解密与响应数据加密，保护敏感信息传输
- **签名验证**：支持 MD5、RSA 两种签名算法，验证请求合法性
- **防重复提交**：基于内存或 Redis 实现请求去重，防止重复提交
- **超时控制**：验证请求时间戳，拒绝超时请求
- **响应签名**：自动为响应生成签名，供客户端验证响应完整性

## 快速集成

### 环境要求

- JDK 1.8+
- Spring Boot 2.7.x

### 引入依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cn.coderxiaoc</groupId>
    <artifactId>api-security-spring-boot-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```

## 核心注解详解

### 1. @Encrypt：响应数据加密

用于对接口返回的响应数据进行加密处理，可标注在类或方法上（方法级优先级更高）。

#### 注解参数

| 参数名         | 类型    | 说明                                       | 默认值   |
| -------------- | ------- | ------------------------------------------ | -------- |
| `value`        | String  | 加密密钥（与`secretKey`互为别名）          | 空字符串 |
| `secretKey`    | String  | 加密密钥（与`value`互为别名）              | 空字符串 |
| `encryptField` | String  | 指定需要加密的字段（为空则加密整个响应体） | 空字符串 |
| `merge`        | boolean | 加密后是否合并回原响应体（替换原字段）     | true     |

### 2. @Decrypt：请求数据解密

用于对接口接收的请求数据进行解密处理，可标注在类或方法上（方法级优先级更高）。

#### 注解参数

| 参数名               | 类型    | 说明                                         | 默认值   |
| -------------------- | ------- | -------------------------------------------- | -------- |
| `value`              | String  | 解密密钥（与`secretKey`互为别名）            | 空字符串 |
| `secretKey`          | String  | 解密密钥（与`value`互为别名）                | 空字符串 |
| `decryptField`       | String  | 指定需要解密的字段（为空则解密整个请求体）   | 空字符串 |
| `decryptResultField` | String  | 解密后从结果中提取的目标字段（用于嵌套结构） | 空字符串 |
| `merge`              | boolean | 解密后是否合并回原请求体（替换原字段）       | true     |

### 3. @Verification：请求签名验证

用于验证请求签名合法性，支持防重复提交和超时控制，仅标注在方法上。

#### 注解参数

| 参数名                  | 类型        | 说明                                 | 默认值      |      |
| ----------------------- | ----------- | ------------------------------------ | ----------- | ---- |
| `value`                 | String      | 签名参数表达式（生成待签名字符串）   | 无（必填）  |      |
| `runtimeType`           | RuntimeType | 验证时机（`BEFORE`/`AFTER`请求解析） | BEFORE      |      |
| `signatureField`        | String      | 请求头中存储签名的字段名             | 无（必填）  |      |
| `preventDuplicateField` | String      | 防重复提交的标识字段（如 nonce）     | 空字符串    |      |
| `timeoutField`          | String      | 超时验证的时间戳字段名               | 空字符串    |      |
| `timeout`               | long        | 超时时间（结合`timeoutField`）       | 0（不启用） |      |
| `delimiter`             | String      | 签名参数拼接分隔符                   | "           | "    |
| `splitter`              | String      | 表达式中参数的分隔符                 | "&"         |      |

### 4. @Signature：响应签名生成

用于为接口响应生成签名并添加到响应头，可标注在类或方法上。

#### 注解参数

| 参数名           | 类型        | 说明                                     | 默认值     |      |
| ---------------- | ----------- | ---------------------------------------- | ---------- | ---- |
| `value`          | String      | 签名参数表达式（生成待签名字符串）       | 无（必填） |      |
| `runtimeType`    | RuntimeType | 签名生成时机（`BEFORE`/`AFTER`响应处理） | BEFORE     |      |
| `signatureField` | String      | 响应头中存储签名的字段名                 | 无（必填） |      |
| `delimiter`      | String      | 签名参数拼接分隔符                       | "          | "    |
| `splitter`       | String      | 表达式中参数的分隔符                     | "&"        |      |

## 签名算法配置

支持 **MD5** 和 **RSA** 两种签名算法，可通过配置指定使用的算法。

### 1. MD5 签名配置

#### 配置参数（前缀：`web-security.signature.md5`）

| 参数名      | 类型   | 说明                 | 默认值 |      |
| ----------- | ------ | -------------------- | ------ | ---- |
| `secretKey` | String | MD5 签名密钥（必填） | 无     |      |
| `delimiter` | String | 签名参数拼接分隔符   | "      | "    |

#### 配置示例

```yaml
web-security:
  signature:
    md5:
      secret-key: "your-md5-secret-key"
      delimiter: "|"
```

### 2. RSA 签名配置

#### 配置参数（前缀：`web-security.signature.rsa`）

| 参数名        | 类型   | 说明                                    | 默认值 |
| ------------- | ------ | --------------------------------------- | ------ |
| `privateKey`  | String | RSA 私钥字符串（与`privatePath`二选一） | 无     |
| `publicKey`   | String | RSA 公钥字符串（与`publicPath`二选一）  | 无     |
| `privatePath` | String | RSA 私钥文件路径（类路径下）            | 无     |
| `publicPath`  | String | RSA 公钥文件路径（类路径下）            | 无     |

#### 配置示例（使用文件路径）

```yaml
web-security:
  signature:
    rsa:
      private-path: "rsa/private.pem"  # 类路径下的私钥文件
      public-path: "rsa/public.pem"    # 类路径下的公钥文件
```

#### 生成 RSA 密钥对

可使用工具类 `RSAUtil` 生成密钥对：

```java
// 生成密钥对字符串
Map<String, String> keyPair = RSAUtil.createKeyPairWithString();
String publicKey = keyPair.get("publicKey");
String privateKey = keyPair.get("privateKey");

// 生成密钥对文件
RSAUtil.createKeyPairWithFile("public.pem", "private.pem");
```

## 防重复提交配置

支持 **内存存储** 和 **Redis 存储** 两种方式，默认使用内存存储。

### 配置参数（前缀：`web-security.signature`）

| 参数名                     | 类型     | 说明                             | 默认值             |
| -------------------------- | -------- | -------------------------------- | ------------------ |
| `enablePreventDuplicate`   | Boolean  | 是否启用防重复提交               | false              |
| `preventDuplicateField`    | String   | 防重复标识字段名（如 x-s-nonce） | "x-s-nonce"        |
| `preventDuplicateTimeout`  | Long     | 防重复过期时间                   | 5                  |
| `preventDuplicateTimeUnit` | TimeUnit | 防重复时间单位                   | SECONDS            |
| `preventDuplicatePrefix`   | String   | 防重复缓存键前缀                 | "preventDuplicate" |
| `enableTimeout`            | Boolean  | 是否启用超时验证                 | false              |
| `timeoutField`             | String   | 时间戳字段名                     | "x-s-timestamp"    |
| `timeout`                  | Long     | 超时时间（秒）                   | 5                  |

### 配置示例（启用 Redis）

需额外引入 Redis 依赖：



```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
web-security:
  signature:
    enable-prevent-duplicate: true
    prevent-duplicate-timeout: 60  # 防重复有效期60秒
    enable-timeout: true
    timeout: 300  # 超时时间300秒（5分钟）
```

## 签名参数表达式

`@Verification` 和 `@Signature` 的 `value` 参数支持 SpEL 表达式，用于提取签名所需参数，支持以下变量：

- `#params.header(key)`：获取请求头参数（如`#params.header('x-s-token')`）
- `#params.body(key)`：获取请求体 / 响应体字段（如`#params.body('data')`）
- `#sing.getNonce(field)`：生成随机串并添加到响应头（如`#sing.getNonce('x-s-nonce')`）
- `#sing.getNonce(field, len)`：生成指定长度的随机串（如`#sing.getNonce('x-s-nonce', 10)`）
- `#sing.getTimestamp(field)`：生成当前时间戳并添加到响应头（如`#sing.getTimestamp('x-s-timestamp')`）



## 异常处理

| 异常类                           | 说明             | 解决方案                            |
| -------------------------------- | ---------------- | ----------------------------------- |
| `MD5Exception`                   | MD5 签名过程异常 | 检查 MD5 密钥是否配置，参数是否合法 |
| `ReadSecretKeyException`         | RSA 密钥读取异常 | 检查密钥文件路径或密钥字符串格式    |
| `SignatureParamsEmptyException`  | 签名参数解析为空 | 检查`value`表达式是否正确提取参数   |
| `InvalidSignatureFieldException` | 签名字段未配置   | 确保`signatureField`参数已设置      |

## 完整使用示例

```java
@RestController
@RequestMapping("/api/v1/trade")
public class TradeController {

    // 完整安全配置：解密请求 + 验证签名 + 加密响应 + 生成响应签名
    @PostMapping("/submit")
    @Decrypt(
        secretKey = "${app.decrypt.key}",
        decryptField = "data",
        decryptResultField = "data"
    )
    @Encrypt(
        secretKey = "${app.encrypt.key}",
        encryptField = "data"
    )
    @Verification(
        runtimeType = RuntimeType.BEFORE,
        value = "#params.header('x-s-token')&#params.header('x-s-nonce')&#params.header('x-s-timestamp')&#params.body('data')",
        signatureField = "x-s-sing",
        preventDuplicateField = "x-s-nonce",
        timeoutField = "x-s-timestamp",
        timeout = 60
    )
    @Signature(
        runtimeType = RuntimeType.AFTER,
        value = "#params.header('x-s-token')&#sing.getNonce('x-s-nonce')&#sing.getTimestamp('x-s-timestamp')&#params.body('data')",
        signatureField = "x-s-sing"
    )
    public Result<TradeResponse> submitTrade(@RequestBody TradeRequest request) {
        // 业务逻辑处理
        return Result.success(new TradeResponse(...));
    }
}
```

# 签名参数表达式详细说明

签名参数表达式是`@Verification`（请求签名验证）和`@Signature`（响应签名生成）注解中`value`参数的核心配置，用于动态提取签名所需的参数并生成待签名字符串。表达式基于**Spring EL（SpEL）** 实现，支持灵活的参数提取、自定义逻辑调用等功能，以下是详细说明：

## 一、表达式基础原理

1. **解析流程**：
   - 表达式通过`ParamsParseAbstract`及其实现类`DefaultParamsParse`解析
   - 首先按`splitter`（默认`&`）分割为多个子表达式
   - 每个子表达式通过 SpEL 解析器解析为具体值
   - 最终通过`delimiter`（默认`|`）拼接所有子表达式的值，生成待签名字符串
2. **核心配置**：
   - `splitter`：表达式分割符，用于分隔多个参数表达式（默认`&`，可通过注解或配置修改）
   - `delimiter`：参数值拼接符，用于拼接解析后的参数值（默认`|`，可通过注解或配置修改）

## 二、表达式可用变量

根据场景（请求验证 / 响应签名）不同，表达式上下文提供的变量有所差异，具体如下：

### 1. 通用变量：`#params`（`SignatureParams`对象）

在请求验证和响应签名场景中均存在，封装了请求 / 响应的头信息和体信息，提供以下方法：

| 方法名        | 说明                                    | 示例                                                  |
| ------------- | --------------------------------------- | ----------------------------------------------------- |
| `header(key)` | 获取指定请求头 / 响应头的值             | `#params.header('x-s-token')` → 获取`x-s-token`头的值 |
| `body(key)`   | 获取请求体 / 响应体中指定字段的值       | `#params.body('userId')` → 获取体中`userId`字段的值   |
| `bodyJson()`  | 将整个请求体 / 响应体转换为 JSON 字符串 | `#params.bodyJson()` → 生成体的 JSON 字符串           |

### 2. 响应签名场景特有变量

在`@Signature`注解（响应签名）中，额外提供以下变量：

| 变量名      | 类型                 | 说明                               | 示例                                             |
| ----------- | -------------------- | ---------------------------------- | ------------------------------------------------ |
| `#request`  | `ServerHttpRequest`  | 请求对象，可获取请求相关信息       | `#request.getURI().getPath()` → 获取请求路径     |
| `#response` | `ServerHttpResponse` | 响应对象，可操作响应头 / 状态码等  | `#response.getHeaders().add('x-s-extra', 'val')` |
| `#sing`     | `SingUtilBean`       | 签名工具类，用于生成随机串和时间戳 | 见下表具体方法                                   |

`#sing`（`SingUtilBean`）提供的方法：

| 方法名                 | 说明                                               | 示例                                                         |
| ---------------------- | -------------------------------------------------- | ------------------------------------------------------------ |
| `getNonce(field)`      | 生成 6 位随机串，添加到响应头`field`中             | `#sing.getNonce('x-s-nonce')` → 生成随机串并添加到`x-s-nonce`头 |
| `getNonce(field, len)` | 生成指定长度（`len`）随机串，添加到响应头`field`中 | `#sing.getNonce('x-s-nonce', 10)` → 生成 10 位随机串         |
| `getTimestamp(field)`  | 生成当前时间戳（毫秒级），添加到响应头`field`中    | `#sing.getTimestamp('x-s-timestamp')` → 生成时间戳并添加到`x-s-timestamp`头 |

### 3. 请求验证场景特有变量

在`@Verification`注解（请求签名验证）中，主要变量为`#params`（同上），专注于提取请求头和请求体中的参数用于签名验证。

## 三、自定义 Bean 方法调用

表达式支持调用 Spring 容器中的自定义 Bean 方法，实现复杂的参数处理逻辑，语法为：`@beanName.methodName(参数)`

### 条件：

1. 自定义 Bean 需通过`@Component`等注解注册到 Spring 容器（如`@Component("myTest")`）
2. 方法参数可引用表达式上下文变量（如`#params`）

### 示例：

1. 定义自定义 Bean：

```java
@Component("mySignUtil")
public class MySignUtil {
    // 自定义参数处理：拼接用户ID和请求时间戳
    public String buildUserTime(SignatureParams params) {
        String userId = params.body("userId");
        String timestamp = params.header("x-s-timestamp");
        return userId + "_" + timestamp;
    }
}
```

1. 在表达式中调用：

```java
@Verification(
    value = "@mySignUtil.buildUserTime(#params)&#params.header('x-s-nonce')",
    signatureField = "x-s-sign"
)
```

解析过程：

- 先调用`mySignUtil`的`buildUserTime`方法，传入`#params`参数
- 再提取`x-s-nonce`请求头的值
- 最终按`delimiter`（默认`|`）拼接两个结果

## 四、表达式示例

### 1. 请求签名验证（`@Verification`）



```java
@Verification(
    // 表达式：提取token、nonce、timestamp请求头，以及请求体中的orderId字段
    value = "#params.header('x-s-token')&#params.header('x-s-nonce')&#params.header('x-s-timestamp')&#params.body('orderId')",
    signatureField = "x-s-sign",
    splitter = "&",  // 显式指定分割符（默认即可）
    delimiter = "|"  // 显式指定拼接符（默认即可）
)
```

解析后生成的待签名字符串格式：`token值|nonce值|timestamp值|orderId值`

### 2. 响应签名生成（`@Signature`）



```java
@Signature(
    // 表达式：提取token请求头，生成nonce和timestamp响应头，拼接响应体JSON
    value = "#params.header('x-s-token')&#sing.getNonce('x-s-nonce')&#sing.getTimestamp('x-s-timestamp')&#params.bodyJson()",
    signatureField = "x-s-sign"
)
```

解析后生成的待签名字符串格式：`token值|生成的nonce值|生成的timestamp值|响应体JSON字符串`

### 3. 结合自定义 Bean

```java
@Signature(
    // 表达式：调用自定义Bean处理参数，再拼接随机串
    value = "@orderSignProcessor.buildSignParams(#params)&#sing.getNonce('x-s-nonce', 8)",
    signatureField = "x-s-sign"
)
```
