package br.com.smartmesquitaapi;

import br.com.smartmesquitaapi.config.crypto.CryptoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SmartMesquitaApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testEncryptDecrypt() {
        String original = "1234567890";
        String b64 = CryptoUtils.encrypt(original);
        assertNotNull(b64);
        String back = CryptoUtils.decrypt(b64);
        assertEquals(original, back);
    }

}
