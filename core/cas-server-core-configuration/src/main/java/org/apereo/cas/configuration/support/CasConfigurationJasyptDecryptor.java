package org.apereo.cas.configuration.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationJasyptDecryptor.class);

    private static final String ENCRYPTED_VALUE_PREFIX = "{cipher}";

    private enum JasyptEncryptionParameters {

        ALGORITHM("cas.standalone.config.security.alg", "PBEWithMD5AndTripleDES"),
        PROVIDER("cas.standalone.config.security.provider", null),
        ITERATIONS("cas.standalone.config.security.iteration", null),
        PASSWORD("cas.standalone.config.security.psw", null);

        private String name;
        private String defaultValue;

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

    private final StandardPBEStringEncryptor decryptor;

    public CasConfigurationJasyptDecryptor(final Environment environment) {
        this.decryptor = new StandardPBEStringEncryptor();

        final String alg = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ALGORITHM);
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.debug("Configured decryptor algorithm [{}]", alg);
            decryptor.setAlgorithm(alg);
        }

        final String psw = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PASSWORD);
        if (StringUtils.isNotBlank(psw)) {
            LOGGER.debug("Configured decryptor password");
            decryptor.setPassword(psw);
        }

        final String pName = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PROVIDER);
        if (StringUtils.isNotBlank(pName)) {
            LOGGER.debug("Configured decryptor provider");
            if (StringUtils.equals(pName, BouncyCastleProvider.PROVIDER_NAME)) {
                Security.addProvider(new BouncyCastleProvider());
            }
            this.decryptor.setProviderName(pName);
        }
        final String iter = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ITERATIONS);
        if (StringUtils.isNotBlank(iter) && NumberUtils.isCreatable(iter)) {
            LOGGER.debug("Configured decryptor iterations");
            decryptor.setKeyObtentionIterations(Integer.valueOf(iter));
        }
    }

    private static String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return environment.getProperty(param.getName(), param.getDefaultValue());
    }

    /**
     * Decrypt map.
     *
     * @param settings the settings
     * @return the map
     */
    public Map<Object, Object> decrypt(final Map<Object, Object> settings) {
        final Map<Object, Object> decrypted = new HashMap<>();
        settings.forEach((key, value1) -> {
            final String stringValue = getStringPropertyValue(value1);
            if (StringUtils.isNotBlank(stringValue) && stringValue.startsWith(ENCRYPTED_VALUE_PREFIX)) {
                try {
                    if (!this.decryptor.isInitialized()) {
                        LOGGER.debug("Initializing decryptor...");
                        this.decryptor.initialize();
                    }
                    final String encValue = stringValue.substring(ENCRYPTED_VALUE_PREFIX.length());
                    LOGGER.debug("Decrypting property [{}]...", key);
                    final String value = this.decryptor.decrypt(encValue);

                    LOGGER.debug("Decrypted property [{}] successfully.", key);
                    decrypted.put(key, value);
                } catch (final Exception e) {
                    LOGGER.error("Could not decrypt property [{}]. Setting will be ignored by CAS", key, e);
                }
            } else {
                decrypted.put(key, value1);
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
