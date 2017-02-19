package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import java.security.GeneralSecurityException;

/**
 * This is {@link U2FAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        return null;
    }

    @Override
    public boolean supports(final Credential credential) {
        return false;
    }
}
