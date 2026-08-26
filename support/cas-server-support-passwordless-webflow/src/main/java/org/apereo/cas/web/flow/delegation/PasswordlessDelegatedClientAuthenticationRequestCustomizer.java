package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordlessDelegatedClientAuthenticationRequestCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class PasswordlessDelegatedClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {
    @Override
    public void customize(final IndirectClient client, final WebContext webContext, final RequestContext requestContext) throws Throwable {
        val currentParams = (Map<String, String>) webContext.getRequestAttribute(OidcConfiguration.CUSTOM_PARAMS).orElseGet(HashMap::new);
        val passwordlessAccount = Objects.requireNonNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class));
        currentParams.put(OidcConfiguration.LOGIN_HINT, passwordlessAccount.getUsername());
        webContext.setRequestAttribute(OidcConfiguration.CUSTOM_PARAMS, currentParams);
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext, final RequestContext requestContext) throws Throwable {
        val passwordlessAccount = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        return client instanceof OidcClient && passwordlessAccount != null;
    }
}
