package org.jasig.cas.support.oauth.authentication;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Principal;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Specific authentication for OAuth.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public class OAuthAuthentication implements Authentication {

    /** Authentication date stamp. */
    private final ZonedDateTime authenticationDate;

    /** Authenticated principal. */
    private final Principal principal;

    /**
     * Default constructor.
     *
     * @param authenticationDate the authentication date
     * @param principal the principal
     */
    public OAuthAuthentication(final ZonedDateTime authenticationDate, final Principal principal) {
        this.authenticationDate = authenticationDate;
        this.principal = principal;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public ZonedDateTime getAuthenticationDate() {
        return authenticationDate;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public List<CredentialMetaData> getCredentials() {
        return null;
    }

    @Override
    public Map<String, HandlerResult> getSuccesses() {
        return null;
    }

    @Override
    public Map<String, Class<? extends Exception>> getFailures() {
        return null;
    }
}
