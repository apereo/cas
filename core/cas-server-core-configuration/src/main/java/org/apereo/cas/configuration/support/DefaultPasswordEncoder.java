package org.apereo.cas.configuration.support;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link DefaultPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPasswordEncoder.class);

    private String encodingAlgorithm;
    private String characterEncoding;

    /**
     * Instantiates a new default password encoder.
     *
     * @param encodingAlgorithm the encoding algorithm
     * @param characterEncoding the character encoding
     */
    DefaultPasswordEncoder(final String encodingAlgorithm, final String characterEncoding) {
        this.encodingAlgorithm = encodingAlgorithm;
        this.characterEncoding = characterEncoding;
    }

    @Override
    public String encode(final CharSequence password) {
        if (password == null) {
            return null;
        }

        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            LOGGER.warn("No encoding algorithm is defined. Password cannot be encoded; Returning null");
            return null;
        }

        final String encodingCharToUse = StringUtils.isNotBlank(this.characterEncoding)
                ? this.characterEncoding : Charset.defaultCharset().name();

        LOGGER.warn("Using {} as the character encoding algorithm to update the digest", encodingCharToUse);
        
        return new String(DigestUtils.getDigest(this.encodingAlgorithm)
                .digest(password.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        return StringUtils.equals(encodedRawPassword, encodedPassword);
    }
}
