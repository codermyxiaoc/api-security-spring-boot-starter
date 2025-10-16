package cn.coderxiaoc.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSAUtils {
    public static Map<String, String> createKeyPairWhitString() throws NoSuchAlgorithmException {
        HashMap<String, String> keyMap = new HashMap<>();

        KeyPairGenerator kpg;
        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);

        KeyPair keyPair = kpg.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        keyMap.put("publicKey", publicKeyStr);

        PrivateKey privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        keyMap.put("privateKey", privateKeyStr);
        return keyMap;
    }
    public static Map<String, String> createKeyPairWhitFile(String publicKeyPath, String privateKeyPath)
            throws NoSuchAlgorithmException, IOException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();


        PublicKey publicKey = keyPair.getPublic();
        String publicKeyPem = createPemString(publicKey.getEncoded(), "PUBLIC KEY");

        PrivateKey privateKey = keyPair.getPrivate();
        String privateKeyPem = createPemString(privateKey.getEncoded(), "PRIVATE KEY");

        savePemFile(publicKeyPath, publicKeyPem);
        savePemFile(privateKeyPath, privateKeyPem);


        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("publicKey", publicKeyPem);
        keyMap.put("privateKey", privateKeyPem);
        return keyMap;
    }

    private static String createPemString(byte[] keyBytes, String keyType) {

        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN ").append(keyType).append("-----\n");
        pemBuilder.append(base64Key).append("\n");
        pemBuilder.append("-----END ").append(keyType).append("-----");

        return pemBuilder.toString();
    }

    private static void savePemFile(String filePath, String pemContent) throws IOException {
        Path path = Paths.get(filePath);
        File parentDir = path.getParent().toFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        Files.write(path, pemContent.getBytes("UTF-8"));
        System.out.println("密钥已保存至：" + filePath);
    }
    public static String readPublicKeyFromByte(byte[] publicKey){
       return  new String(publicKey).replaceAll(
                "-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\\s",
                ""
        );
    }
    public static String readPrivateKeyFromByte(byte[] privateKey) {
         return new String(privateKey).replaceAll(
                "-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s",
                ""
        );


    }


}
