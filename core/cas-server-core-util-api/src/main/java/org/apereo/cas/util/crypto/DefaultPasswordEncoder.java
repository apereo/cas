package org.apereo.cas.util.crypto;

import org.apereo.cas.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.Charset;

/**
 * This is {@link DefaultPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultPasswordEncoder implements PasswordEncoder {
    private final String encodingAlgorithm;

    private final String characterEncoding;

    @Override
    public String encode(final CharSequence password) {
        if (password == null) {
            return null;
        }

        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            LOGGER.warn("No encoding algorithm is defined. Password cannot be encoded;");
            return null;
        }

        val encodingCharToUse = StringUtils.isNotBlank(this.characterEncoding)
            ? this.characterEncoding : Charset.defaultCharset().name();

        LOGGER.debug("Using [{}] as the character encoding algorithm to update the digest", encodingCharToUse);

        try {
            val pswBytes = password.toString().getBytes(encodingCharToUse);
            val encoded = Hex.encodeHexString(DigestUtils.getDigest(this.encodingAlgorithm).digest(pswBytes));
            LOGGER.debug("Encoded password via algorithm [{}] and character-encoding [{}] is [{}]", this.encodingAlgorithm,
                encodingCharToUse, encoded);
            return encoded;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        val encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        val matched = Strings.CS.equals(encodedRawPassword, encodedPassword);
        val msg = String.format("Provided password does%smatch the encoded password",
            BooleanUtils.toString(matched, StringUtils.EMPTY, " not "));
        LOGGER.debug(msg);
        return matched;
    }
}
