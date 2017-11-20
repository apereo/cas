package org.apereo.cas.configuration.support;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link CasConfigurationJasyptDecryptorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
public class CasConfigurationJasyptDecryptorTests {
    @Autowired
    private Environment environment;

    static {
        System.setProperty(CasConfigurationJasyptDecryptor.JasyptEncryptionParameters.PASSWORD.getName(), "P@$$w0rd");
    }

    private CasConfigurationJasyptDecryptor jasypt;

    @Before
    public void setup() {
        this.jasypt = new CasConfigurationJasyptDecryptor(this.environment);
    }

    @Test
    public void verifyDecryptionEncryption() {
        final String result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        final String plain = jasypt.decryptValue(result);
        assertEquals(plain, getClass().getSimpleName());
    }

    @Test
    public void verifyDecryptionEncryptionPairNotNeeded() {
        final Pair<String, Object> pair = Pair.of("keyName", "keyValue");
        final Pair<String, Object> result = jasypt.decryptPair(pair);
        assertNotNull(result);
        assertEquals(result.getKey(), pair.getKey());
        assertEquals(result.getValue(), pair.getValue());

    }

    @Test
    public void verifyDecryptionEncryptionPairFails() {
        final Pair<String, Object> pair = Pair.of("keyName", CasConfigurationJasyptDecryptor.ENCRYPTED_VALUE_PREFIX + "keyValue");
        final Pair<String, Object> result = jasypt.decryptPair(pair);
        assertNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairSuccess() {
        final String value = jasypt.encryptValue("Testing");
        final Pair<String, Object> pair = Pair.of("keyName", CasConfigurationJasyptDecryptor.ENCRYPTED_VALUE_PREFIX + value);
        final Pair<String, Object> result = jasypt.decryptPair(pair);
        assertNotNull(result);
        assertEquals(result.getKey(), pair.getKey());
        assertEquals(result.getValue(), "Testing");
    }
}

