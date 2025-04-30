package org.apereo.cas.gauth.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorPrepareLoginAction extends AbstractMultifactorAuthenticationAction {
    protected final CasConfigurationProperties casProperties;
    protected final OneTimeTokenCredentialRepository repository;
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal(), requestContext);
        val enabled = isMultipleDeviceRegistrationEnabled(requestContext)
            && repository.count(principal.getId()) >= 1
            && MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
        
        MultifactorAuthenticationWebflowUtils.putGoogleAuthenticatorMultipleDeviceRegistrationEnabled(requestContext, enabled);
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(requestContext, repository.get(principal.getId()));
        return null;
    }

    private boolean isMultipleDeviceRegistrationEnabled(final RequestContext requestContext) {
        return tenantExtractor.extract(requestContext)
            .filter(tenantDefinition -> !tenantDefinition.getProperties().isEmpty())
            .map(tenantDefinition -> tenantDefinition.bindProperties().orElseThrow())
            .map(properties -> properties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled())
            .orElseGet(() -> casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled());
    }
}
