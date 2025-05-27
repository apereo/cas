package org.apereo.cas.gauth.web.flow.account;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.CoreGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfilePrepareAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GoogleMultifactorAuthenticationAccountProfilePrepareAction extends ConsumerExecutionAction {
    public GoogleMultifactorAuthenticationAccountProfilePrepareAction(
        final OneTimeTokenCredentialRepository repository,
        final MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider,
        final CasConfigurationProperties casProperties,
        final TenantExtractor tenantExtractor) {
        super(requestContext -> {

            val multipleDeviceRegistrationEnabled = tenantExtractor.extract(requestContext)
                .filter(tenantDefinition -> !tenantDefinition.getProperties().isEmpty())
                .map(TenantDefinition::bindProperties)
                .filter(ConfigurationPropertiesBindingContext::isBound)
                .filter(bindingContext -> bindingContext.containsBindingFor(CoreGoogleAuthenticatorMultifactorProperties.class))
                .map(ConfigurationPropertiesBindingContext::value)
                .map(properties -> properties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled())
                .orElseGet(() -> casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled());
            
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val enabled = (multipleDeviceRegistrationEnabled || repository.count(principal.getId()) == 0)
                && MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
            requestContext.getFlowScope().put("gauthAccountProfileRegistrationEnabled", enabled);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, googleAuthenticatorMultifactorAuthenticationProvider);
        });
    }
}

