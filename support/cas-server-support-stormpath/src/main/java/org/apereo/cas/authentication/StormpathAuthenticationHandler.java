package org.apereo.cas.authentication;

import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.stormpath.credentials.authenticator.StormpathAuthenticator;

/**
 * This is {@link StormpathAuthenticationHandler} that verifies accounts
 * against Stormpath Cloud.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class StormpathAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {
    private String apiKey;
    private String applicationId;
    private String secretkey;

    public StormpathAuthenticationHandler(final String apiKey, final String applicationId,
                                          final String secretkey) {
        this.apiKey = apiKey;
        this.applicationId = applicationId;
        this.secretkey = secretkey;
    }

    @Override
    protected Authenticator<UsernamePasswordCredentials> getAuthenticator(final Credential credential) {
        final StormpathAuthenticator authenticator = new StormpathAuthenticator(this.apiKey,
                this.secretkey, this.applicationId);
        return authenticator;
    }
}
