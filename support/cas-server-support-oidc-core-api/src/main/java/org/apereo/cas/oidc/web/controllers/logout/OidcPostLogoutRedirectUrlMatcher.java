package org.apereo.cas.oidc.web.controllers.logout;

/**
 * controls how the RP-given postLogoutRedirectUrl is matched against a
 * configured logoutUrl of a service (usecase OIDC RP-initiated logout)
 *
 * @author Christian Migowski
 * @since 6.4.0
 */
@FunctionalInterface
public interface OidcPostLogoutRedirectUrlMatcher {

    public static final String BEAN_NAME_POST_LOGOUT_REDIRECTURL_MATCHER = "postLogoutRedirectUrlMatcher";

    boolean matches(String postLogoutRedirectUrl, String configuredUrl);
}
