package org.apereo.cas.authentication.handler.support;

import module java.base;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.PrePostAuthenticationHandler;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
@Setter
public abstract class AbstractPreAndPostProcessingAuthenticationHandler extends AbstractAuthenticationHandler implements PrePostAuthenticationHandler {

    protected PrincipalNameTransformer principalNameTransformer = String::trim;

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

    protected AuthenticationHandlerExecutionResult createHandlerResult(final Credential credential,
                                                                       @Nullable final Principal principal,
                                                                       @Nullable final List<MessageDescriptor> warnings) {
        return new DefaultAuthenticationHandlerExecutionResult(this, credential, principal, warnings);
    }

    protected AuthenticationHandlerExecutionResult createHandlerResult(final Credential credential,
                                                                       final Principal principal) {
        return new DefaultAuthenticationHandlerExecutionResult(this, credential,
            principal, new ArrayList<>());
    }

    protected String transformUsername(final Credential credential) throws Throwable {
        if (StringUtils.isBlank(credential.getId())) {
            throw new AccountNotFoundException("Username is null.");
        }
        LOGGER.debug("Transforming credential username via [{}]", principalNameTransformer.getClass().getName());
        val transformedUsername = principalNameTransformer.transform(credential.getId());
        if (StringUtils.isBlank(transformedUsername)) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        if (credential instanceof final MutableCredential mc) {
            mc.setId(transformedUsername);
        }
        return transformedUsername;
    }
}
