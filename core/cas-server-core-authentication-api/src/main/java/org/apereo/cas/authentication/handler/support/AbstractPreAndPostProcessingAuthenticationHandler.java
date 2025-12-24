package org.apereo.cas.authentication.handler.support;

import module java.base;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PrePostAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.jspecify.annotations.Nullable;

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

    protected AbstractPreAndPostProcessingAuthenticationHandler(final @Nullable String name,
                                                                final PrincipalFactory principalFactory,
                                                                final Integer order) {
        super(name, principalFactory, order);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential, final Service service)
        throws Throwable {
        if (!preAuthenticate(credential)) {
            throw new FailedLoginException();
        }
        return postAuthenticate(credential, doAuthentication(credential, service));
    }

    protected abstract AuthenticationHandlerExecutionResult doAuthentication(Credential credential, Service service)
        throws Throwable;

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
    protected AuthenticationHandlerExecutionResult createHandlerResult(final Credential credential,
                                                                       @Nullable final Principal principal,
                                                                       @Nullable final List<MessageDescriptor> warnings) {
        return new DefaultAuthenticationHandlerExecutionResult(this, credential, principal, warnings);
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
    protected AuthenticationHandlerExecutionResult createHandlerResult(final Credential credential,
                                                                       final Principal principal) {
        return new DefaultAuthenticationHandlerExecutionResult(this, credential,
            principal, new ArrayList<>());
    }
}
