package com.create.llmera.util;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ApiKeyCrypto {
    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final Path keyDir;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public ApiKeyCrypto(Path keyDir) {
        this.keyDir = keyDir;
    }

    public synchronized void init() throws GeneralSecurityException, IOException {
        Files.createDirectories(keyDir);
        Path privateKeyFile = keyDir.resolve("private.key");
        Path publicKeyFile = keyDir.resolve("public.key");

        if (Files.exists(privateKeyFile) && Files.exists(publicKeyFile)) {
            loadKeys(privateKeyFile, publicKeyFile);
        } else {
            generateKeys(privateKeyFile, publicKeyFile);
        }
    }

    private void loadKeys(Path privateKeyFile, Path publicKeyFile) throws IOException, GeneralSecurityException {
        byte[] privateBytes = Base64.getDecoder().decode(Files.readString(privateKeyFile).trim());
        byte[] publicBytes = Base64.getDecoder().decode(Files.readString(publicKeyFile).trim());

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    private void generateKeys(Path privateKeyFile, Path publicKeyFile) throws GeneralSecurityException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        String privateBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        Files.writeString(privateKeyFile, privateBase64);
        Files.writeString(publicKeyFile, publicBase64);

        try {
            Files.setPosixFilePermissions(privateKeyFile, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public String encrypt(String plainText) throws GeneralSecurityException {
        if (plainText == null || plainText.isBlank()) {
            return "";
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedText) throws GeneralSecurityException {
        if (encryptedText == null || encryptedText.isBlank()) {
            return "";
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public String hash(String data) throws GeneralSecurityException {
        if (data == null) {
            data = "";
        }
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verifyHash(String data, String expectedHash) throws GeneralSecurityException {
        String actualHash = hash(data);
        return actualHash.equals(expectedHash);
    }

    public boolean isReady() {
        return privateKey != null && publicKey != null;
    }
}
