package org.apereo.cas.support.wsfederation.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;

import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;

/**
 * This handler authenticates Security token/credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public WsFederationAuthenticationHandler(final String name,
                                             final PrincipalFactory principalFactory,
                                             final Integer order) {
        super(name, principalFactory, order);
    }

    @Override
    public boolean supports(final Credential credentials) {
        return credentials != null && WsFederationCredential.class.isAssignableFrom(credentials.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return WsFederationCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val wsFederationCredentials = (WsFederationCredential) credential;
        if (wsFederationCredentials != null) {
            val attributes = wsFederationCredentials.getAttributes();
            val principal = principalFactory.createPrincipal(wsFederationCredentials.getId(), attributes);
            return this.createHandlerResult(wsFederationCredentials, principal, new ArrayList<>());
        }
        throw new FailedLoginException();
    }

}
