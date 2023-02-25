package chat.cherish.xxim.core.tool;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CoreTool {

    public static byte[] rsaEncode(String rsaPublicKey, String value) {
        try {
            byte[] publicKeyBytes = Base64.decode(rsaPublicKey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] aesEncode(String key, String iv, byte[] bytes) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "AES"
            );
            IvParameterSpec ivSpec = new IvParameterSpec(
                    iv.getBytes(StandardCharsets.UTF_8)
            );
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] aesDecode(String key, String iv, byte[] bytes) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "AES"
            );
            IvParameterSpec ivSpec = new IvParameterSpec(
                    iv.getBytes(StandardCharsets.UTF_8)
            );
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5Encode32(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(32);
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5Encode16(String value) {
        return md5Encode32(value).substring(8, 24);
    }
}
