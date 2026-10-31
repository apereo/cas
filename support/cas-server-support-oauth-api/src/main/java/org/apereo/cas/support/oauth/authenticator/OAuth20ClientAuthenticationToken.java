package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The credentials a client presents to authenticate itself to an OAuth or OpenID Connect endpoint,
 * as an {@link org.springframework.security.web.authentication.AuthenticationConverter} reads them from the request: a client identifier and secret,
 * a code verifier, a refresh token, a client assertion, a certificate, and so on. Once a client authentication
 * provider accepts them, the authenticated token carries the profile of the client, or of the user for
 * the password grant, as its principal.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OAuth20ClientAuthenticationToken extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 5617390151424612394L;

    private final transient HttpServletRequest request;

    private final CredentialSource source;

    private final @Nullable String username;

    private final transient @Nullable Object credentials;

    private final @Nullable OAuth20UserProfile profile;

    @Setter
    private transient @Nullable HttpServletResponse response;

    public OAuth20ClientAuthenticationToken(final HttpServletRequest request, final CredentialSource source,
                                            final @Nullable String username, final @Nullable Object credentials) {
        super(List.of());
        this.request = request;
        this.source = source;
        this.username = username;
        this.credentials = credentials;
        this.profile = null;
        setAuthenticated(false);
    }

    private OAuth20ClientAuthenticationToken(final OAuth20ClientAuthenticationToken token, final OAuth20UserProfile profile) {
        super(List.of());
        this.request = token.getRequest();
        this.response = token.getResponse();
        this.source = token.getSource();
        this.username = token.getUsername();
        this.credentials = token.getCredentials();
        this.profile = profile;
        setAuthenticated(true);
    }

    /**
     * The authenticated token for the given profile.
     *
     * @param profile the profile of the authenticated client or user
     * @return the authenticated token
     */
    public OAuth20ClientAuthenticationToken authenticated(final OAuth20UserProfile profile) {
        return new OAuth20ClientAuthenticationToken(this, profile);
    }

    @Override
    public @Nullable Object getPrincipal() {
        return profile != null ? profile : username;
    }

    /**
     * Where the credentials were found.
     */
    public enum CredentialSource {
        /**
         * Request parameters.
         */
        FORM,
        /**
         * Request headers.
         */
        HEADER,
        /**
         * The client certificate of a mutual TLS connection.
         */
        CERTIFICATE
    }
}
