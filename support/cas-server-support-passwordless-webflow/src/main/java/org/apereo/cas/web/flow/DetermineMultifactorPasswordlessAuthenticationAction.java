package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DetermineMultifactorPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class DetermineMultifactorPasswordlessAuthenticationAction extends BasePasswordlessCasWebflowAction {
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final PrincipalFactory passwordlessPrincipalFactory;

    private final AuthenticationSystemSupport authenticationSystemSupport;
    
    public DetermineMultifactorPasswordlessAuthenticationAction(final CasConfigurationProperties casProperties,
                                                                final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                                final PrincipalFactory passwordlessPrincipalFactory,
                                                                final AuthenticationSystemSupport authenticationSystemSupport) {
        super(casProperties);
        this.multifactorTriggerSelectionStrategy = multifactorTriggerSelectionStrategy;
        this.passwordlessPrincipalFactory = passwordlessPrincipalFactory;
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }
        if (StringUtils.isBlank(user.getPhone()) && StringUtils.isBlank(user.getEmail())) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.invalid.user");
            return error();
        }
        if (multifactorTriggerSelectionStrategy.multifactorAuthenticationTriggers().isEmpty()) {
            LOGGER.debug("No multifactor authentication triggers are available or defined");
            return success();
        }

        if (!shouldActivateMultifactorAuthenticationFor(requestContext, user)) {
            LOGGER.debug("User [{}] is not activated to use CAS-provided multifactor authentication providers. "
                         + "You may wish to re-examine your CAS configuration to enable and allow for multifactor authentication to be "
                         + "combined with passwordless authentication", user);
            return success();
        }

        val authentication = buildAuthentication(user);
        val service = WebUtils.getService(requestContext);

        val result = resolveMultifactorAuthenticationProvider(requestContext, authentication, service);
        if (result.isEmpty()) {
            LOGGER.debug("No CAS-provided multifactor authentication trigger required user [{}] to proceed with MFA. "
                + "CAS will proceed with its normal passwordless authentication flow.", user);
            return success();
        }

        populateContextWithAuthenticationResult(requestContext, authentication, service);
        LOGGER.debug("Proceed with multifactor authentication flow [{}] for user [{}]", result.get(), user);
        return new EventFactorySupport().event(this, result.map(MultifactorAuthenticationProvider::getId).orElse(StringUtils.EMPTY));
    }

    protected Authentication buildAuthentication(final PasswordlessUserAccount user) throws Throwable {
        val userAttributes = CollectionUtils.toMultiValuedMap((Map) user.getAttributes());
        val resolvedPrincipal = authenticationSystemSupport.getPrincipalResolver()
            .resolve(new BasicIdentifiableCredential(user.getUsername()));
        val attributes = CoreAuthenticationUtils.mergeAttributes(userAttributes, resolvedPrincipal.getAttributes());
        val principalId = resolvedPrincipal instanceof NullPrincipal ? user.getUsername() : resolvedPrincipal.getId();
        val principal = passwordlessPrincipalFactory.createPrincipal(principalId, attributes);
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

    protected void populateContextWithAuthenticationResult(final RequestContext requestContext, final Authentication authentication,
                                                           final WebApplicationService service) throws Throwable {
        val builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
        val authenticationResult = builder
            .collect(authentication)
            .build(authenticationSystemSupport.getPrincipalElectionStrategy(), service);
        WebUtils.putAuthenticationResultBuilder(builder, requestContext);
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(authentication, requestContext);
    }

    protected Optional<MultifactorAuthenticationProvider> resolveMultifactorAuthenticationProvider(
        final RequestContext requestContext, final Authentication authentication,
        final WebApplicationService service) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val registeredService = WebUtils.getRegisteredService(requestContext);
            return multifactorTriggerSelectionStrategy.resolve(request, response, registeredService, authentication, service);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }
}
