package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrePostAuthenticationHandler;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.NonNull;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract authentication handler that allows deployers to utilize the bundled
 * AuthenticationHandlers while providing a mechanism to perform tasks before
 * and after authentication.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public abstract class AbstractPreAndPostProcessingAuthenticationHandler extends AbstractAuthenticationHandler implements PrePostAuthenticationHandler {

    public AbstractPreAndPostProcessingAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                             final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) throws GeneralSecurityException, PreventedException {
        if (!preAuthenticate(credential)) {
            throw new FailedLoginException();
        }
        return postAuthenticate(credential, doAuthentication(credential));
    }

    /**
     * Performs the details of authentication and returns an authentication handler result on success.
     *
     * @param credential Credential to authenticate.
     * @return Authentication handler result on success.
     * @throws GeneralSecurityException On authentication failure that is thrown out to the caller of
     *                                  {@link #authenticate(Credential)}.
     * @throws PreventedException       On the indeterminate case when authentication is prevented.
     */
    protected abstract AuthenticationHandlerExecutionResult doAuthentication(Credential credential) throws GeneralSecurityException, PreventedException;

    /**
     * Helper method to construct a handler result
     * on successful authentication events.
     *
     * @param credential the credential on which the authentication was successfully performed.
     *                   Note that this credential instance may be different from what was originally provided
     *                   as transformation of the username may have occurred, if one is in fact defined.
     * @param principal  the resolved principal
     * @param warnings   the warnings
     * @return the constructed handler result
     */
    protected AuthenticationHandlerExecutionResult createHandlerResult(final @NonNull Credential credential,
                                                                       final @NonNull Principal principal,
                                                                       final @NonNull List<MessageDescriptor> warnings) {
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(credential), principal, warnings);
    }

    /**
     * Helper method to construct a handler result
     * on successful authentication events.
     *
     * @param credential the credential on which the authentication was successfully performed.
     *                   Note that this credential instance may be different from what was originally provided
     *                   as transformation of the username may have occurred, if one is in fact defined.
     * @param principal  the resolved principal
     * @return the constructed handler result
     */
    protected AuthenticationHandlerExecutionResult createHandlerResult(final @NonNull Credential credential,
                                                                       final @NonNull Principal principal) {
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(credential),
            principal, new ArrayList<>(0));
    }
}
