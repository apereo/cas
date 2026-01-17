package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.IvGenerator;
import org.jasypt.iv.NoIvGenerator;
import org.jasypt.iv.RandomIvGenerator;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;

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
     * Pattern for algorithms that require an initialization vector.
     * Regex matches all {@code PBEWITHHMACSHA###ANDAES} algorithms that aren't BouncyCastle.
     */
    private static final Pattern ALGS_THAT_REQUIRE_IV_PATTERN = RegexUtils.createPattern("PBEWITHHMACSHA\\d+ANDAES_.*(?<!-BC)$");

    /**
     * The Jasypt instance.
     */
    private final StandardPBEStringEncryptor jasyptInstance;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CasConfigurationJasyptCipherExecutor(@Nullable final String algorithm, @Nullable final String password) {
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

        val initialize = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.INITIALIZATION_VECTOR);
        if (StringUtils.isNotBlank(initialize)) {
            val required = Boolean.parseBoolean(initialize);
            setIvGenerator(required ? new RandomIvGenerator() : new NoIvGenerator());
        }
    }

    /**
     * Gets jasypt param from env.
     *
     * @param environment the environment
     * @param param       the param
     * @return the jasypt param from env
     */
    private static @Nullable String getJasyptParamFromEnv(final Environment environment, final JasyptEncryptionParameters param) {
        return StringUtils.isNotBlank(param.getDefaultValue())
            ? environment.getProperty(param.getPropertyName(), param.getDefaultValue())
            : environment.getProperty(param.getPropertyName());
    }

    /**
     * Return true if the algorithm requires initialization vector.
     * {@code PBEWithDigestAndAES} algorithms (from the JCE Provider of JAVA 8) require an initialization vector.
     * Other algorithms may also use an initialization vector and it will increase the encrypted text's length.
     *
     * @param algorithm the algorithm to check
     * @return true if algorithm requires initialization vector
     */
    private static boolean isVectorInitializationRequiredFor(final String algorithm) {
        return StringUtils.isNotBlank(algorithm) && ALGS_THAT_REQUIRE_IV_PATTERN.matcher(algorithm).matches();
    }

    /**
     * Is value encrypted, and does it start with the required prefix.
     *
     * @param value the value
     * @return true/false
     */
    public static boolean isValueEncrypted(@Nullable final String value) {
        return StringUtils.isNotBlank(value) && value.startsWith(ENCRYPTED_VALUE_PREFIX);
    }

    /**
     * Extract encrypted value as string to decode later.
     *
     * @param value the value
     * @return the string
     */
    public static @Nullable String extractEncryptedValue(@Nullable final String value) {
        return isValueEncrypted(value) ? Objects.requireNonNull(value).substring(ENCRYPTED_VALUE_PREFIX.length()) : value;
    }

    /**
     * Sets algorithm.
     *
     * @param alg the alg
     */
    public void setAlgorithm(@Nullable final String alg) {
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.debug("Configured Jasypt algorithm [{}]", alg);
            jasyptInstance.setAlgorithm(alg);
            val required = isVectorInitializationRequiredFor(alg);
            setIvGenerator(required ? new RandomIvGenerator() : new NoIvGenerator());
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
     * Sets password.
     *
     * @param psw the psw
     */
    public void setPassword(@Nullable final String psw) {
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
    public void setKeyObtentionIterations(@Nullable final String iter) {
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
    public void setProviderName(@Nullable final String pName) {
        if (StringUtils.isNotBlank(pName)) {
            LOGGER.debug("Configured Jasypt provider");
            jasyptInstance.setProviderName(pName);
        }
    }

    @Override
    public @Nullable String encode(@Nullable final String value, final Object[] parameters) {
        return encryptValue(value);
    }

    @Override
    public @Nullable String decode(@Nullable final String value, final Object[] parameters) {
        return decryptValue(value);
    }

    @Override
    public String getName() {
        return "CAS Configuration Jasypt Encryption";
    }

    /**
     * Encrypt value string.
     *
     * @param value   the value
     * @param handler the handler
     * @return the string
     */
    public @Nullable String encryptValue(@Nullable final String value, final Function<Exception, @Nullable String> handler) {
        try {
            return encryptValueAndThrow(value);
        } catch (final Exception e) {
            return handler.apply(e);
        }
    }

    /**
     * Encrypt value as string.
     *
     * @param value the value
     * @return the string
     */
    public @Nullable String encryptValue(@Nullable final String value) {
        return encryptValue(value, e -> {
            LOGGER.warn("Could not encrypt value [{}]", value, e);
            return null;
        });
    }

    /**
     * Decrypt value string.
     *
     * @param value the value
     * @return the string
     */
    public @Nullable String decryptValue(@Nullable final String value) {
        try {
            return decryptValueAndThrow(value);
        } catch (final Exception e) {
            LOGGER.warn("Could not decrypt value [{}]", value, e);
        }
        return null;
    }

    /**
     * Encrypt value string (but don't log error, for use in shell).
     *
     * @param value the value
     * @return the string
     */
    private String encryptValueAndThrow(@Nullable final String value) {
        initializeJasyptInstanceIfNecessary();
        return ENCRYPTED_VALUE_PREFIX + jasyptInstance.encrypt(value);
    }

    /**
     * Decrypt value directly, regardless of prefixes, etc.
     *
     * @param value the value
     * @return the decrypted value, or parameter value as was passed.
     */
    private @Nullable String decryptValueDirect(@Nullable final String value) {
        initializeJasyptInstanceIfNecessary();
        LOGGER.trace("Decrypting value [{}]...", value);
        val result = jasyptInstance.decrypt(value);
        if (StringUtils.isNotBlank(result)) {
            LOGGER.debug("Decrypted value [{}] successfully.", value);
            return result;
        }
        LOGGER.warn("Encrypted value [{}] has no values.", value);
        return value;
    }

    /**
     * Decrypt value string. (but don't log error, for use in shell).
     *
     * @param value the value
     * @return the string
     */
    private @Nullable String decryptValueAndThrow(@Nullable final String value) {
        if (isValueEncrypted(value)) {
            val encValue = extractEncryptedValue(value);
            return decryptValueDirect(encValue);
        }
        return value;
    }

    /**
     * Initialize jasypt instance if necessary.
     */
    private void initializeJasyptInstanceIfNecessary() {
        if (!jasyptInstance.isInitialized()) {
            LOGGER.trace("Initializing Jasypt...");
            jasyptInstance.initialize();
        }
    }

    /**
     * The Jasypt encryption parameters.
     */
    @RequiredArgsConstructor
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
        PASSWORD("cas.standalone.configuration-security.psw", null),
        /**
         * Use (or not) a Jasypt Initialization Vector.
         */
        INITIALIZATION_VECTOR("cas.standalone.configuration-security.initialization-vector", null);

        /**
         * The Name.
         */
        @Getter
        private final String propertyName;

        /**
         * The Default value.
         */
        @Getter
        @Nullable
        private final String defaultValue;
    }
}
