package org.apereo.cas.util.crypto;

import org.apereo.cas.util.gen.HexRandomStringGenerator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This is {@link GlibcCryptPasswordEncoder}.
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

    private String secret;

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

    /**
     * Special note on DES UnixCrypt:
     * In DES UnixCrypt, so first two characters of the encoded password are the salt.
     * <p>
     * When you change your password, the {@code /bin/passwd} program selects a salt based on the time of day.
     * The salt is converted into a two-character string and is stored in the {@code /etc/passwd} file along with the
     * encrypted {@code "password."[10]} In this manner, when you type your password at login time, the same salt is used again.
     * UNIX stores the salt as the first two characters of the encrypted password.
     *
     * @param rawPassword     the raw password as it was provided
     * @param encodedPassword the encoded password.
     * @return true/false
     */
    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        if (StringUtils.isBlank(encodedPassword)) {
            LOGGER.warn("The encoded password provided for matching is null. Returning false");
            return false;
        }
        var providedSalt = StringUtils.EMPTY;
        val lastDollarIndex = encodedPassword.lastIndexOf('$');
        if (lastDollarIndex == -1) {
            providedSalt = encodedPassword.substring(0, 2);
            LOGGER.debug("Assuming DES UnixCrypt as no delimiter could be found in the encoded password provided");
        } else {
            providedSalt = encodedPassword.substring(0, lastDollarIndex);
            LOGGER.debug("Encoded password uses algorithm [{}]", providedSalt.charAt(1));
        }
        var encodedRawPassword = Crypt.crypt(rawPassword.toString(), providedSalt);
        var matched = StringUtils.equals(encodedRawPassword, encodedPassword);
        LOGGER.debug("Provided password does {}match the encoded password", BooleanUtils.toString(matched, StringUtils.EMPTY, "not "));
        return matched;
    }

    private String generateCryptSalt() {
        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            return null;
        }
        val cryptSalt = new StringBuilder();
        if ("1".equals(this.encodingAlgorithm) || "MD5".equals(this.encodingAlgorithm.toUpperCase())) {
            cryptSalt.append("$1$");
            LOGGER.debug("Encoding with MD5 algorithm");
        } else if ("5".equals(this.encodingAlgorithm) || "SHA-256".equals(this.encodingAlgorithm.toUpperCase())) {
            cryptSalt.append("$5$rounds=").append(this.strength).append('$');
            LOGGER.debug("Encoding with SHA-256 algorithm and [{}] rounds", this.strength);
        } else if ("6".equals(this.encodingAlgorithm) || "SHA-512".equals(this.encodingAlgorithm.toUpperCase())) {
            cryptSalt.append("$6$rounds=").append(this.strength).append('$');
            LOGGER.debug("Encoding with SHA-512 algorithm and [{}] rounds", this.strength);
        } else {
            cryptSalt.append(this.encodingAlgorithm);
            LOGGER.debug("Encoding with DES UnixCrypt algorithm as no indicator for another algorithm was found.");
        }

        if (StringUtils.isBlank(this.secret)) {
            LOGGER.debug("No secret was found. Generating a salt with length [{}]", SALT_LENGTH);
            val keygen = new HexRandomStringGenerator(SALT_LENGTH);
            this.secret = keygen.getNewString();
        } else {
            LOGGER.debug("The provided secrect is used as a salt");
        }
        cryptSalt.append(this.secret);
        return cryptSalt.toString();
    }

}
