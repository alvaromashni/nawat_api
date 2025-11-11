package br.com.smartmesquitaapi.config.crypto;

import lombok.Getter;
import lombok.Setter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Getter
@Setter
public final class CryptoUtils {

    private static final String ALG = "AES";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    // 12 bytes IV recomendado para GCM
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    // carrega a chave a partir da variável de ambiente (BASE64).
    private static final SecretKey SECRET_KEY = loadKeyFromEnv();

    private CryptoUtils(){}

    private static SecretKey loadKeyFromEnv(){
        String b64 = System.getenv("ENCRYPTION_KEY_BASE64");
        if (b64 == null || b64.isBlank()){
            throw new IllegalStateException("Env var ENCRYPTION_KEY_BASE64 não encontrada. Gere uma chave e configure-a.");
        }
        byte[] keyBites = Base64.getDecoder().decode(b64);
        return new SecretKeySpec(keyBites, ALG);
    }

    public static String encrypt(String plaintext){

        if (plaintext == null) return null;
        try {

            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, spec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // armazenei IV + cipherText juntos: [iv || ciphertext]
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            byte[] ivAndCipher = byteBuffer.array();

            return Base64.getEncoder().encodeToString(ivAndCipher);

        } catch(Exception e){
            throw new IllegalStateException("Erro ao criptografar dados sensíveis", e);
        }
    }

    public static String decrypt(String b64IvAndCipher) {
        if (b64IvAndCipher == null) return null;
        try {
            byte[] ivAndCipher = Base64.getDecoder().decode(b64IvAndCipher);
            ByteBuffer byteBuffer = ByteBuffer.wrap(ivAndCipher);

            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, spec);

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Falha na autenticação/decifração -> trata como inválido. Não expor mais detalhes.
            throw new IllegalStateException("Erro ao descriptografar dados sensíveis", e);
        }
    }
    // Utility: gerar chave AES-256 e printar em Base64 (uso único, CLI ou dev)
    public static String generateBase64Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALG);
            keyGenerator.init(256); // 256-bit key
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível gerar chave AES", e);
        }
    }

}
