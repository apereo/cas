import java.util.*
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

def byte[] run(final Object... args) {
    def rawPassword = args[0]
    def generatedSalt = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    if (rawPassword == null) {
        return null;
    }

    try {
        encoded = encode(rawPassword.toString());
        logger.debug("Encoded password via [GroovyPasswordEncoder] and character-encoding [UTF-8] is [{}]", encoded);
        return encoded;
    } catch (final Exception e) {
        logger.error(e.getMessage(), e);
    }
    return null;
}

def String encode(String rawPassword) {
    pswBytes = rawPassword.getBytes("UTF-8");
    return Hex.encodeHexString(DigestUtils.getDigest("SHA-1").digest(pswBytes));
}

def Boolean matches(final Object... args) {
    def rawPassword = args[0]
    def encodedPassword = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    encodedRawPassword = StringUtils.isNotBlank(rawPassword) ? encode(rawPassword.toString()) : null;
    matched = StringUtils.equals(encodedRawPassword, encodedPassword);
    logger.debug("Provided password does{}match the encoded password", BooleanUtils.toString(matched, StringUtils.EMPTY, " not "));
    return matched;
}
