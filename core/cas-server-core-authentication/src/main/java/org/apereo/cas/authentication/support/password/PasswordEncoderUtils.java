package org.apereo.cas.authentication.support.password;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.DefaultPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public final class PasswordEncoderUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordEncoderUtils.class);

    private PasswordEncoderUtils() {
    }

    /**
     * New password encoder password encoder.
     *
     * @param properties the properties
     * @return the password encoder
     */
    public static PasswordEncoder newPasswordEncoder(final PasswordEncoderProperties properties) {
        final String type = properties.getType();
        if (StringUtils.isBlank(type)) {
            LOGGER.debug("No password encoder type is defined, and so none shall be created");
            return NoOpPasswordEncoder.getInstance();
        }

        if (type.endsWith(".groovy")) {
            LOGGER.debug("Creating Groovy-based password encoder at [{}]", type);
            return new GroovyPasswordEncoder(properties.getType());
        }

        if (type.contains(".")) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password encoder [{}]", type);
                final Class<PasswordEncoder> clazz = (Class<PasswordEncoder>) Class.forName(type);
                return clazz.newInstance();
            } catch (final Exception e) {
                LOGGER.error("Falling back to a no-op password encoder as CAS has failed to create "
                        + "an instance of the custom password encoder class " + type, e);
                return NoOpPasswordEncoder.getInstance();
            }
        }

        final PasswordEncoderProperties.PasswordEncoderTypes encoderType = PasswordEncoderProperties.PasswordEncoderTypes.valueOf(type);
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
                return new BCryptPasswordEncoder(properties.getStrength(), RandomUtils.getInstanceNative());
            case SCRYPT:
                LOGGER.debug("Creating SCRYPT encoder");
                return new SCryptPasswordEncoder();
            case PBKDF2:
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating PBKDF2 encoder without secret");
                    return new Pbkdf2PasswordEncoder();
                }
                final int hashWidth = 256;
                return new Pbkdf2PasswordEncoder(properties.getSecret(), properties.getStrength(), hashWidth);
            case NONE:
            default:
                LOGGER.debug("No password encoder shall be created given the requested encoder type [{}]", type);
                return NoOpPasswordEncoder.getInstance();
        }
    }
}
