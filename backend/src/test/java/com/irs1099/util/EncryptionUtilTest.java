package com.irs1099.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("test-encryption-key-32-chars-ok!");
    }

    @Test
    void encrypt_andDecrypt_roundTrip() {
        String plainText = "123-45-6789";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_producesUniqueOutput_eachTime() {
        String plainText = "12-3456789";
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);

        // AES-GCM with random IV produces different ciphertext each time
        assertNotEquals(encrypted1, encrypted2);

        // Both decrypt to the same value
        assertEquals(plainText, encryptionUtil.decrypt(encrypted1));
        assertEquals(plainText, encryptionUtil.decrypt(encrypted2));
    }

    @Test
    void encrypt_doesNotContainPlainText() {
        String ssn = "123-45-6789";
        String encrypted = encryptionUtil.encrypt(ssn);

        assertFalse(encrypted.contains(ssn));
    }

    @Test
    void encrypt_handlesEmptyString() {
        String encrypted = encryptionUtil.encrypt("");
        assertEquals("", encryptionUtil.decrypt(encrypted));
    }

    @Test
    void encrypt_handlesSpecialCharacters() {
        String text = "EIN: 12-3456789 (O'Malley & Sons)";
        String encrypted = encryptionUtil.encrypt(text);
        assertEquals(text, encryptionUtil.decrypt(encrypted));
    }

    @Test
    void decrypt_withWrongKey_throws() {
        String encrypted = encryptionUtil.encrypt("123-45-6789");

        EncryptionUtil wrongKeyUtil = new EncryptionUtil("wrong-key-that-is-32-chars-long!");
        assertThrows(RuntimeException.class, () -> wrongKeyUtil.decrypt(encrypted));
    }

    @Test
    void decrypt_withTamperedCiphertext_throws() {
        String encrypted = encryptionUtil.encrypt("123-45-6789");
        String tampered = encrypted.substring(0, encrypted.length() - 2) + "XX";

        assertThrows(RuntimeException.class, () -> encryptionUtil.decrypt(tampered));
    }
}
