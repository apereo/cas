package org.jasig.cas.authentication.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of PasswordEncoder using message digest. Can accept any
 * message digest that the JDK can accept, including MD5 and SHA1. Returns the
 * equivalent Hash you would get from a Perl digest.
 *
 * @author Scott Battaglia
 * @author Stephen More
 * @since 3.1
 */
@Component("defaultPasswordEncoder")
public final class DefaultPasswordEncoder implements PasswordEncoder {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                                                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int HEX_RIGHT_SHIFT_COEFFICIENT = 4;
    private static final int HEX_HIGH_BITS_BITWISE_FLAG = 0x0f;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPasswordEncoder.class);

    private final String encodingAlgorithm;

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
            final MessageDigest messageDigest = MessageDigest.getInstance(this.encodingAlgorithm);

            final String encodingCharToUse = StringUtils.isNotBlank(this.characterEncoding)
                    ? this.characterEncoding : Charset.defaultCharset().name();

            LOGGER.warn("Using {} as the character encoding algorithm to update the digest", encodingCharToUse);
            messageDigest.update(password.getBytes(encodingCharToUse));

            final byte[] digest = messageDigest.digest();

            return getFormattedText(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes the raw bytes from the digest and formats them correct.
     *
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    private static String getFormattedText(final byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int j = 0; j < bytes.length; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> HEX_RIGHT_SHIFT_COEFFICIENT) & HEX_HIGH_BITS_BITWISE_FLAG]);
            buf.append(HEX_DIGITS[bytes[j] & HEX_HIGH_BITS_BITWISE_FLAG]);
        }
        return buf.toString();
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }
}
