package org.apereo.cas.configuration.support;

import com.google.common.collect.Sets;
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
@Tag("Simple")
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
     * Test all algorithms and verify that algorithms known not to work still don't work.
     * https://sourceforge.net/p/jasypt/bugs/32/
     */
    @Test
    public void verifyAlgorithms() {
        val algorithms = (Set<String>) AlgorithmRegistry.getAllPBEAlgorithms();
        val goodAlgorithms = Sets.difference(algorithms, CasConfigurationJasyptCipherExecutor.ALGORITHM_BLACKLIST_SET);

        for (val algorithm : goodAlgorithms) {
            assertTrue(isAlgorithmFunctional(algorithm));
        }
        for (val algorithm : CasConfigurationJasyptCipherExecutor.ALGORITHM_BLACKLIST_SET) {
            assertFalse(isAlgorithmFunctional(algorithm));
        }
    }

    private boolean isAlgorithmFunctional(final String algorithm) {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setAlgorithmForce(algorithm);
        val testValue = "Testing_" + algorithm;
        val value = jasyptTest.encryptValue(testValue);
        val result = jasyptTest.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        return testValue.equals(result);
    }
}

