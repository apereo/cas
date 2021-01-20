package org.apereo.cas.configuration.support;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.registry.AlgorithmRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

import java.util.Set;

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
@Tag("CasConfiguration")
public class CasConfigurationJasyptCipherExecutorTests {
    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
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
        assertEquals(plain, getClass().getSimpleName());
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
     * This seeks to ensure that a password encrypted in a previous version of CAS can still be decrypted.
     * Password encrypted with 6.3.0 shell and password of "P@$$w0rd".
     */
    @Test
    public void verifyOldEncryptedPasswordStillWorks() {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setAlgorithmForce("PBEWITHSHAAND256BITAES-CBC-BC");
        assertEquals("testing", jasyptTest.decode("{cas-cipher}GxXRraiiFRMNDS81OAs6eo6qnhfHdfY1LrggFHRhfQo="));
    }

    /**
     * This seeks to ensure that a password encrypted with an initialization vector still works.
     * Password encrypted with 6.4.0 and password of "P@$$w0rd".
     */
    @Test
    public void verifyEncryptedPasswordWithInitizializationVectorStillWorks() {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setProviderName("BC");
        jasyptTest.setAlgorithmForce("PBEWITHSHAAND256BITAES-CBC-BC");
        jasyptTest.configureInitializationVector();
        assertEquals("testing", jasyptTest.decode("{cas-cipher}88HKpXCD888/ZP7hMAg7VdxljZD3fho5r5V7c15kPXovYCk4cBdpcxfd5vgcxTit"));
    }

    /**
     * Test all algorithms that should work without an initialization vector.
     */
    @Test
    public void verifyAlgorithmsWithoutInitializationVector() {
        val algorithms = (Set<String>) AlgorithmRegistry.getAllPBEAlgorithms();
        for (val algorithm : algorithms) {
            if (!algorithm.matches(CasConfigurationJasyptCipherExecutor.ALGS_THAT_REQUIRE_IV_PATTERN)) {
                assertTrue(isAlgorithmFunctional(algorithm, false));
            }
        }
    }

    /**
     * Test all algorithms with an initialization vector.
     */
    @Test
    public void verifyAlgorithmsWithInitializationVector() {
        val algorithms = (Set<String>) AlgorithmRegistry.getAllPBEAlgorithms();
        for (val algorithm : algorithms) {
            assertTrue(isAlgorithmFunctional(algorithm, true));
        }
    }

    private boolean isAlgorithmFunctional(final String algorithm, final boolean useInitializationVector) {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setAlgorithmForce(algorithm);
        if (useInitializationVector) {
            jasyptTest.configureInitializationVector();
        }
        val testValue = "Testing_" + algorithm;
        val value = jasyptTest.encryptValue(testValue);
        val result = jasyptTest.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        return testValue.equals(result);
    }
}

