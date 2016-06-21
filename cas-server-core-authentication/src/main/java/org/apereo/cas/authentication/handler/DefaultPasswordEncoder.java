package org.apereo.cas.authentication.handler;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DefaultPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPasswordEncoder.class);
    
    private String encodingAlgorithm;
    private String characterEncoding;
    
    public DefaultPasswordEncoder() {
    }

    /**
     * Instantiates a new default password encoder.
     *
     * @param encodingAlgorithm the encoding algorithm
     */
    public DefaultPasswordEncoder(final String encodingAlgorithm) {
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
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        return StringUtils.equals(encodedRawPassword, encodedPassword);
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public void setEncodingAlgorithm(final String encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }
}
