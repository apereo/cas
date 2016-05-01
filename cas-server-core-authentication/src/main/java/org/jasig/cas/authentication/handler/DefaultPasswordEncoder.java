package org.jasig.cas.authentication.handler;

import org.jasig.cas.util.DigestUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Implementation of PasswordEncoder using message digest. Can accept any
 * message digest that the JDK can accept, including MD5 and SHA1. Returns the
 * equivalent Hash you would get from a Perl digest.
 *
 * @author Scott Battaglia
 * @author Stephen More
 * @since 3.1
 */
@RefreshScope
@Component("defaultPasswordEncoder")
public class DefaultPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPasswordEncoder.class);

    private String encodingAlgorithm;

    @Value("${cas.authn.password.encoding.char:}")
    private String characterEncoding;

    /**
     * Instantiates a new default password encoder.
     *
     * @param encodingAlgorithm the encoding algorithm
     */
    @Autowired
    public DefaultPasswordEncoder(@Value("${cas.authn.password.encoding.alg:}") final String encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }

    @Override
    public String encode(final String password) {
        if (password == null) {
            return null;
        }

        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            LOGGER.warn("No encoding algorithm is defined. Password cannot be encoded; Returning null");
            return null;
        }

        try {
            final String encodingCharToUse = StringUtils.isNotBlank(this.characterEncoding)
                    ? this.characterEncoding : Charset.defaultCharset().name();

            LOGGER.warn("Using {} as the character encoding algorithm to update the digest", encodingCharToUse);

            return DigestUtils.digest(this.encodingAlgorithm, password.getBytes(encodingCharToUse));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String encodedRawPassword = (rawPassword != null) ? encode(rawPassword.toString()) : null;
        return StringUtils.equals(encodedRawPassword, encodedPassword);
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }
}
