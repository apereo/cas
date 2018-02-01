package org.apereo.cas.configuration.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CipherExecutor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.core.env.Environment;

import java.security.Security;

/**
 * This is {@link CasConfigurationJasyptCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@Slf4j
public class CasConfigurationJasyptCipherExecutor implements CipherExecutor<String, String> {
    /**
     * Prefix inserted at the beginning of a value to indicate it's encrypted.
     */
    public static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";

    /**
     * The Jasypt encryption parameters.
     */
    public enum JasyptEncryptionParameters {

        /**
         * Jasypt algorithm name to use.
         */
        ALGORITHM("cas.standalone.configurationSecurity.alg", "PBEWithMD5AndTripleDES"),
        /**
         * Jasypt provider name to use.
         */
        PROVIDER("cas.standalone.configurationSecurity.provider", null),
        /**
         * Jasypt number of iterations to use.
         */
        ITERATIONS("cas.standalone.configurationSecurity.iteration", null),
        /**
         * Jasypt password to use.
         */
        PASSWORD("cas.standalone.configurationSecurity.psw", null);

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

        /**
         * Instantiates a new Jasypt encryption parameters.
         *
         * @param name         the name
         * @param defaultValue the default value
         */
        JasyptEncryptionParameters(final String name, final String defaultValue) {
            this.propertyName = name;
            this.defaultValue = defaultValue;
        }
    }

    /**
     * The Jasypt instance.
     */
    private final StandardPBEStringEncryptor jasyptInstance;

    /**
     * Instantiates a new Cas configuration jasypt cipher executor.
     *
     * @param environment the environment
     */
    public CasConfigurationJasyptCipherExecutor(final Environment environment) {
        Security.addProvider(new BouncyCastleProvider());
        this.jasyptInstance = new StandardPBEStringEncryptor();

        final String alg = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ALGORITHM);
        setAlgorithm(alg);
        final String psw = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PASSWORD);
        setPassword(psw);
        final String pName = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PROVIDER);
        setProviderName(pName);
        final String iter = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ITERATIONS);
        setKeyObtentionIterations(iter);
    }

    /**
     * Sets algorithm.
     *
     * @param alg the alg
     */
    public void setAlgorithm(final String alg) {
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.debug("Configured Jasypt algorithm [{}]", alg);
            jasyptInstance.setAlgorithm(alg);
        }
    }

    /**
     * Sets password.
     *
     * @param psw the psw
     */
    public void setPassword(final String psw) {
        if (StringUtils.isNotBlank(psw)) {
            LOGGER.debug("Configured Jasypt password");
            jasyptInstance.setPassword(psw);
        }
    }

    /**
     * Sets key obtention iterations.
     *
     * @param iter the iter
     */
    public void setKeyObtentionIterations(final String iter) {
        if (StringUtils.isNotBlank(iter) && NumberUtils.isCreatable(iter)) {
            LOGGER.debug("Configured Jasypt iterations");
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
            LOGGER.debug("Configured Jasypt provider");
            this.jasyptInstance.setProviderName(pName);
        }
    }

    @Override
    public String encode(final String value) {
        return encryptValue(value);
    }

    @Override
    public String decode(final String value) {
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
            return encryptValuePropagateExceptions(value);
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
    public String encryptValuePropagateExceptions(final String value) {
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
            return decryptValuePropagateExceptions(value);
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
    public String decryptValuePropagateExceptions(final String value) {
        if (StringUtils.isNotBlank(value) && value.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            initializeJasyptInstanceIfNecessary();

            final String encValue = value.substring(ENCRYPTED_VALUE_PREFIX.length());
            LOGGER.trace("Decrypting value [{}]...", encValue);
            final String result = this.jasyptInstance.decrypt(encValue);

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
            LOGGER.debug("Initializing Jasypt...");
            this.jasyptInstance.initialize();
        }
    }

    /**
     * Gets jasypt param from env.
     *
     * @param environment the environment
     * @param param       the param
     * @return the jasypt param from env
     */
    private static String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return environment.getProperty(param.getPropertyName(), param.getDefaultValue());
    }
}
