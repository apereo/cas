package org.apereo.cas.configuration.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link CasConfigurationJasyptCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@Slf4j
public class CasConfigurationJasyptCipherExecutorTests {
    @Autowired
    private Environment environment;

    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
    }

    private CasConfigurationJasyptCipherExecutor jasypt;

    @Before
    public void setup() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
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
        final String result = jasypt.decryptValue("keyValue");
        assertNotNull(result);
        assertEquals("keyValue", result);

    }

    @Test
    public void verifyDecryptionEncryptionPairFails() {
        final String encVal = CasConfigurationJasyptCipherExecutor.ENCRYPTED_VALUE_PREFIX + "keyValue";
        final String result = jasypt.decode(encVal);
        assertNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairSuccess() {
        final String value = jasypt.encryptValue("Testing");
        final String result = jasypt.decode(value);
        assertNotNull(result);
        assertEquals("Testing", result);
    }
}

