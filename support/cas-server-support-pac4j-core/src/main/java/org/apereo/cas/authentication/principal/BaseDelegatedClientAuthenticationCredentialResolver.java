package org.apereo.cas.authentication.principal;

import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link BaseDelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public abstract class BaseDelegatedClientAuthenticationCredentialResolver
    implements DelegatedClientAuthenticationCredentialResolver {

    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    public boolean supports(final ClientCredential credentials) {
        return credentials != null;
    }

    protected Optional<UserProfile> resolveUserProfile(final RequestContext requestContext, final ClientCredential credentials) {
        return Optional.ofNullable(credentials.getUserProfile())
            .or(() -> {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                val webContext = new JEEContext(request, response);
                val client = configContext.getIdentityProviders()
                    .findClient(credentials.getClientName(), webContext)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to locate client"));

                val callContext = new CallContext(webContext, configContext.getSessionStore());
                return client.getUserProfile(callContext, credentials.getCredentials());
            });
    }
}
