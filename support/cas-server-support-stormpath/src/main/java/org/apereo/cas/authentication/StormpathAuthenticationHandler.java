package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
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

    private final String apiKey;
    private final String applicationId;
    private final String secretkey;

    public StormpathAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                          final Integer order, final String apiKey, final String applicationId,
                                          final String secretkey) {
        super(name, servicesManager, principalFactory, order);
        this.apiKey = apiKey;
        this.applicationId = applicationId;
        this.secretkey = secretkey;
    }

    @Override
    protected Authenticator<UsernamePasswordCredentials> getAuthenticator(final Credential credential) {
        return new StormpathAuthenticator(this.apiKey, this.secretkey, this.applicationId);
    }
}
