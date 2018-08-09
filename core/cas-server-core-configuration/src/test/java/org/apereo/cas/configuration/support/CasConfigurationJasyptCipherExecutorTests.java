package org.apereo.cas.configuration.support;

import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link CasConfigurationJasyptCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasConfigurationJasyptCipherExecutorTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
    }

    @Autowired
    private Environment environment;
    private CasConfigurationJasyptCipherExecutor jasypt;

    @Before
    public void initialize() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
    }

    @Test
    public void verifyDecryptionEncryption() {
        val result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        val plain = jasypt.decryptValue(result);
        assertEquals(plain, getClass().getSimpleName());
    }

    @Test
    public void verifyDecryptionEncryptionPairNotNeeded() {
        val result = jasypt.decryptValue("keyValue");
        assertNotNull(result);
        assertEquals("keyValue", result);

    }

    @Test
    public void verifyDecryptionEncryptionPairFails() {
        val encVal = CasConfigurationJasyptCipherExecutor.ENCRYPTED_VALUE_PREFIX + "keyValue";
        val result = jasypt.decode(encVal, new Object[]{});
        assertNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairSuccess() {
        val value = jasypt.encryptValue("Testing");
        val result = jasypt.decode(value, new Object[]{});
        assertNotNull(result);
        assertEquals("Testing", result);
    }
}

