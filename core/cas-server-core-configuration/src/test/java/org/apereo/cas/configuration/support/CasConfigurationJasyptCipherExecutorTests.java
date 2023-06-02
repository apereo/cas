package org.apereo.cas.configuration.support;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.registry.AlgorithmRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationJasyptCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("Cipher")
public class CasConfigurationJasyptCipherExecutorTests {
    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.INITIALIZATION_VECTOR.getPropertyName(), "true");
    }

    @Autowired
    private Environment environment;

    private CasConfigurationJasyptCipherExecutor jasypt;

    @BeforeEach
    public void initialize() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
    }

    @Test
    public void verifyDecryptionEncryption() {
        val result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        val plain = jasypt.decryptValue(result);
        assertEquals(getClass().getSimpleName(), plain);
    }

    @Test
    public void verifyEncodeOps() {
        assertNotNull(jasypt.getName());
        val result = jasypt.encode(getClass().getSimpleName());
        assertNotNull(result);
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
        val result = jasypt.decode(encVal, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairSuccess() {
        val value = jasypt.encryptValue("Testing");
        val result = jasypt.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNotNull(result);
        assertEquals("Testing", result);
    }

    /**
     * This seeks to ensure that a password encrypted with an initialization vector still works.
     * Password encrypted with 6.4.0 and password of "P@$$w0rd".
     */
    @Test
    public void verifyEncryptedPassword() {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setProviderName("BC");
        jasyptTest.setAlgorithm("PBEWITHSHAAND256BITAES-CBC-BC");
        jasyptTest.setIvGenerator(new RandomIvGenerator());
        assertEquals("testing", jasyptTest.decode("{cas-cipher}88HKpXCD888/ZP7hMAg7VdxljZD3fho5r5V7c15kPXovYCk4cBdpcxfd5vgcxTit"));
    }

    @Test
    public void verifyAlgorithms() {
        val algorithms = AlgorithmRegistry.getAllPBEAlgorithms();
        for (val algorithm : algorithms) {
            assertTrue(isAlgorithmFunctional(algorithm.toString()));
        }
    }

    private boolean isAlgorithmFunctional(final String algorithm) {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setAlgorithm(algorithm);
        val testValue = "Testing_" + algorithm;
        val value = jasyptTest.encryptValue(testValue);
        val result = jasyptTest.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        return testValue.equals(result);
    }
}

