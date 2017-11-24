package org.apereo.cas.configuration.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CipherExecutor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

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
    public static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";

    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationJasyptCipherExecutor.class);

    /**
     * The Jasypt encryption parameters.
     */
    public enum JasyptEncryptionParameters {

        /**
         * Jasypt algorithm name to use.
         */
        ALGORITHM("cas.standalone.config.security.alg", "PBEWithMD5AndTripleDES"),
        /**
         * Jasypt provider name to use.
         */
        PROVIDER("cas.standalone.config.security.provider", null),
        /**
         * Jasypt number of iterations to use.
         */
        ITERATIONS("cas.standalone.config.security.iteration", null),
        /**
         * Jasypt password to use.
         */
        PASSWORD("cas.standalone.config.security.psw", null);

        /**
         * The Name.
         */
        private final String name;
        /**
         * The Default value.
         */
        private final String defaultValue;

        /**
         * Instantiates a new Jasypt encryption parameters.
         *
         * @param name         the name
         * @param defaultValue the default value
         */
        JasyptEncryptionParameters(final String name, final String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets default value.
         *
         * @return the default value
         */
        public String getDefaultValue() {
            return defaultValue;
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
            initializeJasyptInstanceIfNecessary();
            return this.jasyptInstance.encrypt(value);
        } catch (final Exception e) {
            LOGGER.error("Could not encrypt value [{}]", e);
        }
        return null;
    }


    /**
     * Decrypt value string.
     *
     * @param value the value
     * @return the string
     */
    public String decryptValue(final String value) {
        try {
            initializeJasyptInstanceIfNecessary();
            return this.jasyptInstance.decrypt(value);
        } catch (final Exception e) {
            LOGGER.error("Could not decrypt value [{}]", e);
        }
        return null;
    }

    /**
     * Decrypt key/value string.
     *
     * @param pair the value
     * @return the string
     */
    public Pair<String, Object> decryptPair(final Pair<String, Object> pair) {
        try {
            final String stringValue = getStringPropertyValue(pair.getValue());
            if (StringUtils.isNotBlank(stringValue) && stringValue.startsWith(ENCRYPTED_VALUE_PREFIX)) {
                initializeJasyptInstanceIfNecessary();
                
                try {
                    final String encValue = stringValue.substring(ENCRYPTED_VALUE_PREFIX.length());
                    LOGGER.debug("Decrypting property [{}]...", pair.getKey());
                    final String value = decryptValue(encValue);

                    if (StringUtils.isNotBlank(value)) {
                        LOGGER.debug("Decrypted property [{}] successfully.", pair.getKey());
                        return Pair.of(pair.getKey(), value);
                    }
                    LOGGER.warn("Decrypted property [{}] has no values.", pair.getKey());
                    return null;
                } catch (final Exception e) {
                    LOGGER.error("Could not decrypt property [{}].", pair.getKey(), e);
                }
            }
            return pair;
        } catch (final Exception e) {
            LOGGER.error("Could not decrypt value [{}]", e);
        }
        return null;
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
     * Decrypt map.
     *
     * @param settings the settings
     * @return the map
     */
    public Map<String, Object> decrypt(final Map<String, Object> settings) {
        final Map<String, Object> decrypted = new HashMap<>();
        settings.forEach((key, value) -> {
            final Pair<String, Object> pair = decryptPair(Pair.of(key, value));
            if (pair != null) {
                decrypted.put(pair.getKey(), pair.getValue());
            } else {
                LOGGER.error("CAS will ignore [{}] as it could not process it", key);
            }
        });
        return decrypted;
    }

    /**
     * Retrieves the {@link String} of an {@link Object}.
     *
     * @param propertyValue The property value to cast
     * @return A {@link String} representing the property value or {@code null} if it is not a {@link String}
     */
    private static String getStringPropertyValue(final Object propertyValue) {
        return propertyValue instanceof String ? propertyValue.toString() : null;
    }

    /**
     * Gets jasypt param from env.
     *
     * @param environment the environment
     * @param param       the param
     * @return the jasypt param from env
     */
    private static String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return environment.getProperty(param.getName(), param.getDefaultValue());
    }
}
