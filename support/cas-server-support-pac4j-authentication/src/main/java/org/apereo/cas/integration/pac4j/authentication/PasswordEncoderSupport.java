package org.apereo.cas.integration.pac4j.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.credentials.password.ShiroPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link PasswordEncoderSupport}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PasswordEncoderSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordEncoderSupport.class);

    protected PasswordEncoderSupport() {
    }

    /**
     * New password encoder password encoder.
     *
     * @param passwordEncoder the password encoder
     * @return the password encoder
     */
    public static PasswordEncoder newPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {

        if (StringUtils.isNotBlank(passwordEncoder.getEncodingAlgorithm())) {
            final DefaultHashService hash = new DefaultHashService();
            hash.setHashAlgorithmName(passwordEncoder.getEncodingAlgorithm());
            hash.setHashIterations(passwordEncoder.getStrength());
            hash.setPrivateSalt(ByteSource.Util.bytes(passwordEncoder.getSecret().getBytes()));
            final DefaultPasswordService svc = new DefaultPasswordService();
            svc.setHashService(hash);

            LOGGER.debug("Creating an instance of Shiro password encoder for delegated pac4j authentication. " +
                            "Hash algorithm {}, hash iterations {}", passwordEncoder.getEncodingAlgorithm(),
                    passwordEncoder.getStrength());
            return new ShiroPasswordEncoder(svc);
        }

        LOGGER.debug("No hash algorithm is defined, so no pac4j password encoder may be created");
        return null;
    }
}
