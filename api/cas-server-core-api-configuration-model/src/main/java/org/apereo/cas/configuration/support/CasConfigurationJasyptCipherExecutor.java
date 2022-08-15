package org.apereo.cas.configuration.support;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.IvGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.core.env.Environment;

import java.security.Security;

/**
 * This is {@link CasConfigurationJasyptCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationJasyptCipherExecutor implements CipherExecutor<String, String> {
    /**
     * Prefix inserted at the beginning of a value to indicate it's encrypted.
     */
    public static final String ENCRYPTED_VALUE_PREFIX = "{cas-cipher}";

    /**
     * The Jasypt instance.
     */
    private final StandardPBEStringEncryptor jasyptInstance;

    public CasConfigurationJasyptCipherExecutor(final String algorithm, final String password) {
        Security.addProvider(new BouncyCastleProvider());
        jasyptInstance = new StandardPBEStringEncryptor();
        setIvGenerator(new RandomIvGenerator());
        setAlgorithm(algorithm);
        setPassword(password);
    }

    public CasConfigurationJasyptCipherExecutor(final Environment environment) {
        this(getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ALGORITHM),
            getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PASSWORD));
        val pName = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PROVIDER);
        setProviderName(pName);
        val iter = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ITERATIONS);
        setKeyObtentionIterations(iter);
    }

    private static String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return environment.getProperty(param.getPropertyName(), param.getDefaultValue());
    }

    /**
     * Sets algorithm.
     *
     * @param alg the alg
     */
    public void setAlgorithm(final String alg) {
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.trace("Configured Jasypt algorithm [{}]", alg);
            jasyptInstance.setAlgorithm(alg);
            configureInitializationVector();
        }
    }

    /**
     * Sets iv generator.
     *
     * @param iv the iv
     */
    public void setIvGenerator(final IvGenerator iv) {
        jasyptInstance.setIvGenerator(iv);
    }

    /**
     * {@code PBEWithDigestAndAES} algorithms (from the JCE Provider of JAVA 8) require an initialization vector.
     * Other algorithms may also use an initialization vector and it will increase the encrypted text's length.
     */
    private void configureInitializationVector() {

    }

    /**
     * Sets password.
     *
     * @param psw the psw
     */
    public void setPassword(final String psw) {
        if (StringUtils.isNotBlank(psw)) {
            LOGGER.trace("Configured Jasypt password");
            jasyptInstance.setPasswordCharArray(psw.toCharArray());
        }
    }

    /**
     * Sets key obtention iterations.
     *
     * @param iter the iter
     */
    public void setKeyObtentionIterations(final String iter) {
        if (StringUtils.isNotBlank(iter) && NumberUtils.isCreatable(iter)) {
            LOGGER.trace("Configured Jasypt iterations");
            jasyptInstance.setKeyObtentionIterations(Integer.parseInt(iter));
        }
    }

    /**
     * Sets provider name.
     *
     * @param pName the p name
     */
    public void setProviderName(final String pName) {
        if (StringUtils.isNotBlank(pName)) {
            LOGGER.trace("Configured Jasypt provider");
            this.jasyptInstance.setProviderName(pName);
        }
    }

    @Override
    public String encode(final String value, final Object[] parameters) {
        return encryptValue(value);
    }

    @Override
    public String decode(final String value, final Object[] parameters) {
        return decryptValue(value);
    }

    @Override
    public String getName() {
        return "CAS Configuration Jasypt Encryption";
    }

    /**
     * Encrypt value string.
     *
     * @param value the value
     * @return the string
     */
    public String encryptValue(final String value) {
        try {
            return encryptValueUnchecked(value);
        } catch (final Exception e) {
            LOGGER.error("Could not encrypt value [{}]", value, e);
        }
        return null;
    }

    /**
     * Encrypt value string (but don't log error, for use in shell).
     *
     * @param value the value
     * @return the string
     */
    public String encryptValueUnchecked(final String value) {
        initializeJasyptInstanceIfNecessary();
        return ENCRYPTED_VALUE_PREFIX + this.jasyptInstance.encrypt(value);
    }

    /**
     * Decrypt value string.
     *
     * @param value the value
     * @return the string
     */
    public String decryptValue(final String value) {
        try {
            return decryptValueUnchecked(value);
        } catch (final Exception e) {
            LOGGER.error("Could not decrypt value [{}]", value, e);
        }
        return null;
    }

    /**
     * Decrypt value string. (but don't log error, for use in shell).
     *
     * @param value the value
     * @return the string
     */
    public String decryptValueUnchecked(final String value) {
        if (StringUtils.isNotBlank(value) && value.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            initializeJasyptInstanceIfNecessary();

            val encValue = value.substring(ENCRYPTED_VALUE_PREFIX.length());
            LOGGER.trace("Decrypting value [{}]...", encValue);
            val result = this.jasyptInstance.decrypt(encValue);

            if (StringUtils.isNotBlank(result)) {
                LOGGER.debug("Decrypted value [{}] successfully.", encValue);
                return result;
            }
            LOGGER.warn("Encrypted value [{}] has no values.", encValue);
        }
        return value;
    }


    /**
     * Initialize jasypt instance if necessary.
     */
    private void initializeJasyptInstanceIfNecessary() {
        if (!this.jasyptInstance.isInitialized()) {
            LOGGER.trace("Initializing Jasypt...");
            jasyptInstance.initialize();
        }
    }

    /**
     * The Jasypt encryption parameters.
     */
    public enum JasyptEncryptionParameters {

        /**
         * Jasypt algorithm name to use.
         */
        ALGORITHM("cas.standalone.configuration-security.alg", "PBEWithMD5AndTripleDES"),
        /**
         * Jasypt provider name to use. None for Java, {@code BC} for BouncyCastle.
         */
        PROVIDER("cas.standalone.configuration-security.provider", null),
        /**
         * Jasypt number of iterations to use.
         */
        ITERATIONS("cas.standalone.configuration-security.iterations", null),
        /**
         * Jasypt password to use for encryption and decryption.
         */
        PASSWORD("cas.standalone.configuration-security.psw", null);

        /**
         * The Name.
         */
        @Getter
        private final String propertyName;

        /**
         * The Default value.
         */
        @Getter
        private final String defaultValue;

        JasyptEncryptionParameters(final String name, final String defaultValue) {
            this.propertyName = name;
            this.defaultValue = defaultValue;
        }
    }
}
