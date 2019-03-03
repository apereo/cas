package org.apereo.cas.util.crypto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * this is {@link GlibcCryptPasswordEncoder}.
 *
 * @author Martin Böhmer
 * @since 5.3.10
 */
@Slf4j
@AllArgsConstructor
public class GlibcCryptPasswordEncoder implements PasswordEncoder {

    private static final int SALT_LENGTH = 8;

    private final String encodingAlgorithm;
    private final int strength;
    private final String secret;

    @Override
    public String encode(final CharSequence password) {
        if (password == null) {
            return null;
        }

        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            LOGGER.warn("No encoding algorithm is defined. Password cannot be encoded; Returning null");
            return null;
        }

        return Crypt.crypt(password.toString(), generateCryptSalt());
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        final String encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        final boolean matched = StringUtils.equals(encodedRawPassword, encodedPassword);
        LOGGER.debug("Provided password does{}match the encoded password", BooleanUtils.toString(matched, StringUtils.EMPTY, " not "));
        return matched;
    }

    private String generateCryptSalt() {
        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            return null;
        }
        final StringBuilder cryptSalt = new StringBuilder();
        if ("1".equals(this.encodingAlgorithm) || "MD5".equals(this.encodingAlgorithm.toUpperCase())) {
            // MD5
            cryptSalt.append("$1$");
        } else if ("5".equals(this.encodingAlgorithm) || "SHA-256".equals(this.encodingAlgorithm.toUpperCase())) {
            // SHA-256
            cryptSalt.append("$5$");
            cryptSalt.append("rounds=").append(this.strength).append('$');
        } else if ("6".equals(this.encodingAlgorithm) || "SHA-512".equals(this.encodingAlgorithm.toUpperCase())) {
            // SHA-512
            cryptSalt.append("$6$");
            cryptSalt.append("rounds=").append(this.strength).append('$');
        } else {
            // UNIX Crypt algorithm
            cryptSalt.append(this.encodingAlgorithm);
        }
        // Add real salt
        if (StringUtils.isBlank(this.secret)) {
            final Base64RandomStringGenerator keygen = new Base64RandomStringGenerator(SALT_LENGTH);
            cryptSalt.append(keygen.getNewString()).append('$');
        } else {
            cryptSalt.append(secret).append('$');
        }
        return cryptSalt.toString();
    }

}
