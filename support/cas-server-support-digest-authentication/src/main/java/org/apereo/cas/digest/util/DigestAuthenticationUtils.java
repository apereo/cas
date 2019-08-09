package org.apereo.cas.digest.util;

import org.apereo.cas.util.RandomUtils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.auth.DigestScheme;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link DigestAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@UtilityClass
public class DigestAuthenticationUtils {

    /**
     * Create nonce string.
     *
     * @return the nonce
     */
    public static String createNonce() {
        val fmtDate = ZonedDateTime.now(ZoneOffset.UTC).toString();
        val rand = RandomUtils.getNativeInstance();
        val randomInt = rand.nextInt();
        return DigestUtils.md5Hex(fmtDate + randomInt);
    }

    /**
     * Create c-nonce string.
     *
     * @return the cnonce
     */
    public static String createCnonce() {
        return DigestScheme.createCnonce();
    }

    /**
     * Create opaque.
     *
     * @param domain the domain
     * @param nonce  the nonce
     * @return the opaque
     */
    public static String createOpaque(final String domain, final String nonce) {
        return DigestUtils.md5Hex(domain + nonce);
    }

    /**
     * Create authenticate header, containing the realm, nonce, opaque, etc.
     *
     * @param realm      the realm
     * @param authMethod the auth method
     * @param nonce      the nonce
     * @return the header string
     */
    public static String createAuthenticateHeader(final String realm, final String authMethod, final String nonce) {
        val stringBuilder = new StringBuilder("Digest realm=\"")
            .append(realm).append("\",");
        if (StringUtils.isNotBlank(authMethod)) {
            stringBuilder.append("qop=").append(authMethod).append(',');
        }
        return stringBuilder.append("nonce=\"").append(nonce)
            .append("\",opaque=\"").append(createOpaque(realm, nonce))
            .append('"')
            .toString();
    }
}
