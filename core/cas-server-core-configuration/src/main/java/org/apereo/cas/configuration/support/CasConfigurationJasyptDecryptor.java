package org.apereo.cas.configuration.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasConfigurationJasyptDecryptor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationJasyptDecryptor {
    /**
     * Prefix inserted at the beginning of a value to indicate it's encrypted.
     */
    public static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationJasyptDecryptor.class);
    
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

        private final String name;
        private final String defaultValue;

        JasyptEncryptionParameters(final String name, final String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    private final StandardPBEStringEncryptor jasyptInstance;

    public CasConfigurationJasyptDecryptor(final Environment environment) {
        this.jasyptInstance = new StandardPBEStringEncryptor();

        final String alg = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ALGORITHM);
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.debug("Configured jasyptInstance algorithm [{}]", alg);
            jasyptInstance.setAlgorithm(alg);
        }

        final String psw = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PASSWORD);
        if (StringUtils.isNotBlank(psw)) {
            LOGGER.debug("Configured jasyptInstance password");
            jasyptInstance.setPassword(psw);
        }

        final String pName = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PROVIDER);
        if (StringUtils.isNotBlank(pName)) {
            LOGGER.debug("Configured jasyptInstance provider");
            if (StringUtils.equals(pName, BouncyCastleProvider.PROVIDER_NAME)) {
                Security.addProvider(new BouncyCastleProvider());
            }
            this.jasyptInstance.setProviderName(pName);
        }
        final String iter = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ITERATIONS);
        if (StringUtils.isNotBlank(iter) && NumberUtils.isCreatable(iter)) {
            LOGGER.debug("Configured jasyptInstance iterations");
            jasyptInstance.setKeyObtentionIterations(Integer.parseInt(iter));
        }
    }

    private static String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return environment.getProperty(param.getName(), param.getDefaultValue());
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
}
