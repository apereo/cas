package org.jasig.cas.support.openid.web.support;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface OpenIdUserNameExtractor {

    /**
     * Extract local username from uri.
     *
     * @param uri the uri
     * @return the username
     */
    String extractLocalUsernameFromUri(String uri);
}
