package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link InitPasswordResetAction}, serves a as placeholder for extensions.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class InitPasswordResetAction extends BaseCasWebflowAction {
    private final PasswordManagementService passwordManagementService;
    private final CasConfigurationProperties casProperties;
    private final PrincipalResolver principalResolver;
    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final MultifactorAuthenticationContextValidator multifactorAuthenticationContextValidator;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val username = getPasswordResetUsername(requestContext);
        if (StringUtils.isBlank(username)) {
            LOGGER.error("Password reset token could not be verified to determine username");
            return error();
        }

        if (doesPasswordResetRequireMultifactorAuthentication(requestContext)) {
            val resolvedPrincipal = resolvedPrincipal(username);
            val provider = selectMultifactorAuthenticationProvider(requestContext, resolvedPrincipal);
            if (!doesMultifactorAuthenticationProviderExistInContext(requestContext, provider)) {
                val deviceManager = provider.getDeviceManager();
                if (deviceManager != null && !deviceManager.hasRegisteredDevices(resolvedPrincipal)) {
                    LOGGER.warn("No registered devices for multifactor authentication could be found for [{}] via [{}]", resolvedPrincipal.getId(), provider.getId());
                    return error();
                }
                return routeToMultifactorAuthenticationProvider(requestContext, resolvedPrincipal, provider);
            }
        }
        val credential = new UsernamePasswordCredential();
        credential.setUsername(username);
        WebUtils.putCredential(requestContext, credential);
        return success();
    }

    protected Event routeToMultifactorAuthenticationProvider(final RequestContext requestContext,
                                                             final Principal resolvedPrincipal,
                                                             final MultifactorAuthenticationProvider provider) {
        val authentication = DefaultAuthenticationBuilder.newInstance().setPrincipal(resolvedPrincipal).build();
        WebUtils.putAuthentication(authentication, requestContext);
        val builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
        val authenticationResult = builder.collect(authentication);
        WebUtils.putAuthenticationResultBuilder(authenticationResult, requestContext);
        WebUtils.putTargetTransition(requestContext, CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, provider);
        return eventFactory.event(this, provider.getId(),
            new LocalAttributeMap<>(Map.of(MultifactorAuthenticationProvider.class.getName(), provider)));
    }

    protected Principal resolvedPrincipal(final String username) throws Throwable {
        val resolvedPrincipal = principalResolver.resolve(new BasicIdentifiableCredential(username));
        return resolvedPrincipal instanceof NullPrincipal
            ? authenticationSystemSupport.getPrincipalFactory().createPrincipal(username)
            : resolvedPrincipal;
    }

    protected String getPasswordResetUsername(final RequestContext requestContext) {
        val token = PasswordManagementWebflowUtils.getPasswordResetToken(requestContext);
        if (StringUtils.isNotBlank(token)) {
            return passwordManagementService.parseToken(token);
        }
        val request = PasswordManagementWebflowUtils.getPasswordResetRequest(requestContext);
        return request != null ? request.getUsername() : null;
    }

    protected boolean doesMultifactorAuthenticationProviderExistInContext(final RequestContext requestContext,
                                                                          final MultifactorAuthenticationProvider provider) {
        val authResultBuilder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        return authResultBuilder != null && authResultBuilder.getAuthentications().stream().anyMatch(authentication -> {
            val result = multifactorAuthenticationContextValidator.validate(authentication, provider.getId(), Optional.ofNullable(registeredService));
            return result.isSuccess() && result.getProvider().isPresent();
        });
    }


    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(final RequestContext requestContext,
                                                                                        final Principal principal) throws Throwable {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        return multifactorAuthenticationProviderSelector.resolve(providers.values(), registeredService, principal);
    }

    protected boolean doesPasswordResetRequireMultifactorAuthentication(final RequestContext requestContext) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val providerId = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(requestContext);
        return casProperties.getAuthn().getPm().getReset().isMultifactorAuthenticationEnabled()
            && !providers.isEmpty() && StringUtils.isBlank(providerId);
    }
}
