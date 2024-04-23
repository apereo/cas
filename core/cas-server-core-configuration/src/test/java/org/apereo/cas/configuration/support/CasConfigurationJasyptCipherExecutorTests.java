package org.apereo.cas.configuration.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.registry.AlgorithmRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("Cipher")
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
    public void initialize() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
    }

    @Test
    void verifyDecryptionEncryption() throws Throwable {
        val result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        val plain = jasypt.decryptValue(result);
        assertEquals(getClass().getSimpleName(), plain);
    }

    @Test
    void verifyEncodeOps() throws Throwable {
        assertNotNull(jasypt.getName());
        val result = jasypt.encode(getClass().getSimpleName());
        assertNotNull(result);
    }

    @Test
    void verifyDecryptionEncryptionPairNotNeeded() throws Throwable {
        val result = jasypt.decryptValue("keyValue");
        assertNotNull(result);
        assertEquals("keyValue", result);

    }

    @Test
    void verifyDecryptionEncryptionPairFails() throws Throwable {
        val encVal = CasConfigurationJasyptCipherExecutor.ENCRYPTED_VALUE_PREFIX + "keyValue";
        val result = jasypt.decode(encVal, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNull(result);
    }

    @Test
    void verifyDecryptionEncryptionPairSuccess() throws Throwable {
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
    void verifyEncryptedPassword() throws Throwable {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setProviderName("BC");
        jasyptTest.setAlgorithm("PBEWITHSHAAND256BITAES-CBC-BC");
        jasyptTest.setIvGenerator(new RandomIvGenerator());
        assertEquals("testing", jasyptTest.decode("{cas-cipher}88HKpXCD888/ZP7hMAg7VdxljZD3fho5r5V7c15kPXovYCk4cBdpcxfd5vgcxTit"));
    }

    @Test
    void verifyAlgorithms() throws Throwable {
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

