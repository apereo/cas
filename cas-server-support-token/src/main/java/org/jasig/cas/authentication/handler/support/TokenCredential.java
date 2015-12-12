package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.BasicIdentifiableCredential;
import org.jasig.cas.authentication.token.Token;
import org.jasig.cas.authentication.token.TokenAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * This is {@link TokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenCredential extends BasicIdentifiableCredential {
    private static final long serialVersionUID = 2749515041385101770L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenCredential.class);

    @NotNull
    private Token token;

    @NotNull
    private String username;

    @NotNull
    private String service;

    private Map<String, Object> userAttributes;

    /**
     * Instantiates a new Token credential.
     *
     * @param username the username
     * @param tokenId    the token
     * @param service  the service
     */
    public TokenCredential(final String username, final String tokenId, final String service) {
        super(tokenId);
        this.token = new Token(tokenId);
        this.service = service;
        this.username = username;
    }

    public final void setToken(final Token token) {
        this.token = token;
    }

    public final Token getToken() {
        return this.token;
    }

    public final String getService() {
        return this.service;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public final void setUsername(final String username) {
        this.username = username;
    }

    public final String getUsername() {
        return this.username;
    }

    public final Map<String, Object> getUserAttributes() {
        return this.userAttributes;
    }

    public void setUserAttributes(final TokenAttributes userProfile) {
        this.userAttributes = userProfile;
    }
}
