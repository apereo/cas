package org.apereo.cas.authentication.support.password;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.DefaultPasswordEncoder;
import org.apereo.cas.util.crypto.GlibcCryptPasswordEncoder;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

/**
 * This is {@link PasswordEncoderUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class PasswordEncoderUtils {
    private static final int HASH_WIDTH = 256;

    /**
     * New password encoder password encoder.
     *
     * @param properties         the properties
     * @param applicationContext the application context
     * @return the password encoder
     */
    @SuppressWarnings("java:S5344")
    public static PasswordEncoder newPasswordEncoder(final PasswordEncoderProperties properties,
                                                     final ApplicationContext applicationContext) {
        val type = properties.getType();
        if (StringUtils.isBlank(type)) {
            LOGGER.trace("No password encoder type is defined, and so none shall be created");
            return NoOpPasswordEncoder.getInstance();
        }

        if (type.endsWith(".groovy")) {
            LOGGER.trace("Creating Groovy-based password encoder at [{}]", type);
            val resource = applicationContext.getResource(type);
            return new GroovyPasswordEncoder(resource, applicationContext);
        }

        if (type.contains(".")) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password encoder [{}]", type);
                val clazz = (Class<PasswordEncoder>) Class.forName(type);
                return clazz.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                val msg = "Falling back to a no-op password encoder as CAS has failed to create "
                    + "an instance of the custom password encoder class " + type;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(msg, e);
                } else {
                    LOGGER.error(msg);
                }
                return NoOpPasswordEncoder.getInstance();
            }
        }

        val encoderType = PasswordEncoderProperties.PasswordEncoderTypes.valueOf(type);
        switch (encoderType) {
            case DEFAULT:
                LOGGER.debug("Creating default password encoder with encoding alg [{}] and character encoding [{}]",
                    properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
                return new DefaultPasswordEncoder(properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
            case STANDARD:
                LOGGER.debug("Creating standard password encoder with the secret defined in the configuration");
                return new StandardPasswordEncoder(properties.getSecret());
            case BCRYPT:
                LOGGER.debug("Creating BCRYPT password encoder given the strength [{}] and secret in the configuration",
                    properties.getStrength());
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating BCRYPT encoder without secret");
                    return new BCryptPasswordEncoder(properties.getStrength());
                }
                LOGGER.debug("Creating BCRYPT encoder with secret");
                return new BCryptPasswordEncoder(properties.getStrength(), RandomUtils.getNativeInstance());
            case SCRYPT:
                LOGGER.debug("Creating SCRYPT encoder");
                return new SCryptPasswordEncoder();
            case SSHA:
                LOGGER.warn("Creating SSHA encoder; digest based password encoding is not considered secure. "
                    + "This strategy is here to support legacy implementations and using it is considered insecure.");
                return new LdapShaPasswordEncoder();
            case PBKDF2:
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.trace("Creating PBKDF2 encoder without secret");
                    return new Pbkdf2PasswordEncoder();
                }
                return new Pbkdf2PasswordEncoder(properties.getSecret(), properties.getStrength(), HASH_WIDTH);
            case GLIBC_CRYPT:
                val hasSecret = StringUtils.isNotBlank(properties.getSecret());
                LOGGER.debug("Creating glibc CRYPT encoder with encoding alg [{}], strength [{}] and {}secret",
                    properties.getEncodingAlgorithm(), properties.getStrength(),
                    BooleanUtils.toString(hasSecret, StringUtils.EMPTY, "without "));
                return new GlibcCryptPasswordEncoder(properties.getEncodingAlgorithm(), properties.getStrength(), properties.getSecret());
            case NONE:
            default:
                LOGGER.trace("No password encoder shall be created given the requested encoder type [{}]", type);
                return NoOpPasswordEncoder.getInstance();
        }
    }
}
