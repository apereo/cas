package org.apereo.cas.util.crypto;

import java.security.SecureRandom;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

    /*
     * from https://github.com/apache/commons-codec/blob/dd5e46203e5b12f4b65e76ef2e5c34197692b5b4/src/main/java/org/apache/commons/codec/digest/B64.java#L39
     */
    static final String B64T_STRING = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final String encodingAlgorithm;
    private final int strength;
    private final String secret;

    @Override
    public String encode(CharSequence password) {
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
        String encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
        boolean matched = StringUtils.equals(encodedRawPassword, encodedPassword);
        LOGGER.debug("Provided password does{}match the encoded password", BooleanUtils.toString(matched, StringUtils.EMPTY, " not "));
        return matched;
    }

    private String generateCryptSalt() {
        if (StringUtils.isBlank(this.encodingAlgorithm)) {
            return null;
        }
        StringBuilder cryptSalt = new StringBuilder();
        if ("1".equals(this.encodingAlgorithm) || "MD5".equals(this.encodingAlgorithm.toUpperCase())) {
            // MD5
            cryptSalt.append("$1$");
        } else if ("5".equals(this.encodingAlgorithm) || "SHA-256".equals(this.encodingAlgorithm.toUpperCase())) {
            // SHA-256
            cryptSalt.append("$5$");
            cryptSalt.append("rounds=").append(this.strength).append("$");
        } else if ("6".equals(this.encodingAlgorithm) || "SHA-512".equals(this.encodingAlgorithm.toUpperCase())) {
            // SHA-512
            cryptSalt.append("$6$");
            cryptSalt.append("rounds=").append(this.strength).append("$");
        } else {
            // UNIX Crypt algorithm
            cryptSalt.append(this.encodingAlgorithm);
        }
        // Add real salt
        if (StringUtils.isBlank(this.secret)) {
            cryptSalt.append(getRandomSalt(8, new SecureRandom())).append("$");
        } else {
            cryptSalt.append(secret).append("$");
        }
        return cryptSalt.toString();
    }

    /*
     * from https://github.com/apache/commons-codec/blob/dd5e46203e5b12f4b65e76ef2e5c34197692b5b4/src/main/java/org/apache/commons/codec/digest/B64.java#L95
     */
    private String getRandomSalt(final int num, final Random random) {
        final StringBuilder saltString = new StringBuilder(num);
        for (int i = 1; i <= num; i++) {
            saltString.append(B64T_STRING.charAt(random.nextInt(B64T_STRING.length())));
        }
        return saltString.toString();
    }

}
