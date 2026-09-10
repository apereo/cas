package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
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
@Slf4j
@RequiredArgsConstructor
public class PasswordlessDelegatedClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {
    private final CasConfigurationProperties casProperties;

    @Override
    public void customize(final IndirectClient client, final WebContext webContext, final RequestContext requestContext) {
        val passwordlessAccount = Objects.requireNonNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class));
        val loginHint = Strings.CI.appendIfMissing(passwordlessAccount.getUsername(), '@' + casProperties.getServer().getScope());
        webContext.setRequestAttribute(OidcConfiguration.LOGIN_HINT, loginHint);
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext, final RequestContext requestContext) {
        val passwordlessAccount = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        return client instanceof OidcClient && passwordlessAccount != null;
    }
}
