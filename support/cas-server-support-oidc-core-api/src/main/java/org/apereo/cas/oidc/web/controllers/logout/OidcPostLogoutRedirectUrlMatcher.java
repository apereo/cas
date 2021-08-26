package org.apereo.cas.oidc.web.controllers.logout;

/**
 * Controls how the RP-given postLogoutRedirectUrl is matched against a
 * configured logoutUrl of a service (usecase OIDC RP-initiated logout).
 *
 * @author Christian Migowski
 * @since 6.4.0
 */
@FunctionalInterface
public interface OidcPostLogoutRedirectUrlMatcher {

    /**
     * Default bean name for the implementation.
     */
    String BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER = "postLogoutRedirectUrlMatcher";

    /**
     * Matches the urls and returns the result.
     *
     * @param postLogoutRedirectUrl the post logout redirect url
     * @param configuredUrl         the configured url
     * @return true/false
     */
    boolean matches(String postLogoutRedirectUrl, String configuredUrl);
}
