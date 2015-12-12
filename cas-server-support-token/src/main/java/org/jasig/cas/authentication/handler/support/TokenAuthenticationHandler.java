package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.authentication.token.Token;
import org.jasig.cas.authentication.token.TokenKey;
import org.jasig.cas.authentication.token.TokenKeystore;
import org.jasig.cas.integration.pac4j.authentication.handler.support.TokenWrapperAuthenticationHandler;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.security.auth.login.CredentialNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link org.jasig.cas.authentication.principal.PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenAuthenticationHandler extends TokenWrapperAuthenticationHandler {
    @NotNull
    @Autowired
    @Qualifier("tokenAuthenticationKeystore")
    private TokenKeystore keystore;

    /**
     * A list of required attributes that will be passed along to the
     * {@link org.jasig.cas.authentication.token.TokenAttributes} instance.
     **/
    @NotNull
    @Autowired(required=false)
    @Qualifier("requiredTokenAuthenticationAttributes")
    private List requiredTokenAttributes = new ArrayList();

    /**
     * A map of attribute names to {@link org.jasig.cas.authentication.token.TokenAttributes}
     * properties that will be passed along.
     **/
    @NotNull
    @Autowired(required=false)
    @Qualifier("tokenAuthenticationAttributesMap")
    private Map tokenAttributesMap = new HashMap();

    /**
     * Maximum amount of time (before or after current time) that the 'generated' parameter
     * in the supplied token can differ from the server.
     **/
    private int maxDrift;

    /**
     * Initialize.
     */
    @PostConstruct
    public void initialize() {
        final JwtAuthenticator tokenAuthenticator = new JwtAuthenticator();
        setAuthenticator(tokenAuthenticator);
    }

    @Override
    protected boolean preAuthenticate(final Credential credential) {
        final TokenCredential tokenCredential = (TokenCredential) credential;
        final TokenKey apiKey = this.keystore.get(tokenCredential.getService());
        if (apiKey == null) {
            logger.warn("API key not found in the token keystore for [{}]", tokenCredential.getService());
            throw new RuntimeException(new CredentialNotFoundException("error.authentication.credentials.bad.token.apikey"));
        }

        // Configure the credential's token so that it can be decrypted.
        final Token token = tokenCredential.getToken();
        token.setKey(apiKey);
        token.setRequiredTokenAttributes(this.requiredTokenAttributes);
        token.setTokenAttributesMap(this.tokenAttributesMap);
        tokenCredential.setToken(token);

        try {
            tokenCredential.setUserAttributes(token.getAttributes());
        } catch (final Exception e) {
            logger.warn("Could not decrypt token", e);
            throw new RuntimeException(new CredentialNotFoundException("error.authentication.credentials.bad.token.key"));
        }

        if (!token.getAttributes().isValid()) {
            logger.warn("Invalid token attributes detected.");
            throw new RuntimeException(new CredentialNotFoundException("error.authentication.credentials.missing.required.attributes"));
        }

        return super.preAuthenticate(credential);
    }

    @Autowired(required=false)
    @Override
    public void setPrincipalNameTransformer(@Qualifier("tokenPrincipalNameTransformer")
                                            final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
    }

    public TokenKeystore getKeystore() {
        return keystore;
    }

    public List getRequiredTokenAttributes() {
        return requiredTokenAttributes;
    }

    public Map getTokenAttributesMap() {
        return tokenAttributesMap;
    }

    public int getMaxDrift() {
        return maxDrift;
    }
}
