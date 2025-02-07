package org.apereo.cas.configuration.support;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.registry.AlgorithmRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationJasyptCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("Cipher")
@ExtendWith(CasTestExtension.class)
@Slf4j
class CasConfigurationJasyptCipherExecutorTests {
    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.INITIALIZATION_VECTOR.getPropertyName(), "true");
    }

    @Autowired
    private Environment environment;

    private CasConfigurationJasyptCipherExecutor jasypt;

    @BeforeEach
    void initialize() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
    }

    @Test
    void verifyDecryptionEncryption() {
        val result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        val plain = jasypt.decryptValue(result);
        assertEquals(getClass().getSimpleName(), plain);
    }

    @Test
    void verifyEncodeOps() {
        assertNotNull(jasypt.getName());
        val result = jasypt.encode(getClass().getSimpleName());
        assertNotNull(result);
    }

    @Test
    void verifyDecryptionEncryptionPairNotNeeded() {
        val result = jasypt.decryptValue("keyValue");
        assertNotNull(result);
        assertEquals("keyValue", result);

    }

    @Test
    void verifyDecryptionEncryptionPairFails() {
        val encVal = CasConfigurationJasyptCipherExecutor.ENCRYPTED_VALUE_PREFIX + "keyValue";
        val result = jasypt.decode(encVal, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNull(result);
    }

    @Test
    void verifyDecryptionEncryptionPairSuccess() {
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
    void verifyEncryptedPassword() {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setProviderName("BC");
        jasyptTest.setAlgorithm("PBEWITHSHAAND256BITAES-CBC-BC");
        jasyptTest.setIvGenerator(new RandomIvGenerator());
        jasyptTest.setPassword("P@$$w0rd");
        assertEquals("testing", jasyptTest.decode("{cas-cipher}BvHnbgPin/9TaT4fgctwmtrZzwdRQWGUolr3dS1peGETCWFJOVYgu/Fkg+lxm6QX"));
    }

    @Test
    void verifyAlgorithms() {
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
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("[{}] cannot be encoded via [{}]", testValue, algorithm);
            return true;
        }
        val result = jasyptTest.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        return testValue.equals(result);
    }
}

