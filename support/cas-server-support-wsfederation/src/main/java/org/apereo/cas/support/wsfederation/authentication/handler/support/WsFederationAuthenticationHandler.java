package org.apereo.cas.support.wsfederation.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;

import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This handler authenticates Security token/credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public WsFederationAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                             final PrincipalFactory principalFactory,
                                             final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    /**
     * Determines if this handler can support the credentials provided.
     *
     * @param credentials the credentials to test
     * @return true if supported, otherwise false
     */
    @Override
    public boolean supports(final Credential credentials) {
        return credentials != null && WsFederationCredential.class.isAssignableFrom(credentials.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return WsFederationCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val wsFederationCredentials = (WsFederationCredential) credential;
        if (wsFederationCredentials != null) {
            val attributes = wsFederationCredentials.getAttributes();
            val principal = this.principalFactory.createPrincipal(wsFederationCredentials.getId(), (Map) attributes);
            return this.createHandlerResult(wsFederationCredentials, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException();
    }

}
