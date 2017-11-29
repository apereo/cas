package org.apereo.cas.util.crypto;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.Charset;

/**
 * This is {@link DefaultPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultPasswordEncoder implements PasswordEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPasswordEncoder.class);

    private final String encodingAlgorithm;
    private final String characterEncoding;

    /**
     * Instantiates a new default password encoder.
     *
     * @param encodingAlgorithm the encoding algorithm
     * @param characterEncoding the character encoding
     */
    public DefaultPasswordEncoder(final String encodingAlgorithm, final String characterEncoding) {
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

        LOGGER.debug("Using [{}] as the character encoding algorithm to update the digest", encodingCharToUse);

        try {
            final byte[] pswBytes = password.toString().getBytes(encodingCharToUse);
            final String encoded = Hex.encodeHexString(DigestUtils.getDigest(this.encodingAlgorithm).digest(pswBytes));
            LOGGER.debug("Encoded password via algorithm [{}] and character-encoding [{}] is [{}]", this.encodingAlgorithm,
                    encodingCharToUse, encoded);
            return encoded;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        final boolean matched = StringUtils.equals(encodedRawPassword, encodedPassword);
        LOGGER.debug("Provided password does{}match the encoded password", BooleanUtils.toString(matched, StringUtils.EMPTY, " not "));
        return matched;
    }
}
