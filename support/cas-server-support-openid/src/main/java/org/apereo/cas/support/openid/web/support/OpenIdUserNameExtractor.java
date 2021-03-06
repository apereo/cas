package org.apereo.cas.support.openid.web.support;

/**
 * Extract the userid for OpenID protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@FunctionalInterface
@Deprecated(since = "6.2.0")
public interface OpenIdUserNameExtractor {

    /**
     * Extract local username from uri.
     *
     * @param uri the uri
     * @return the username
     */
    String extractLocalUsernameFromUri(String uri);
}
