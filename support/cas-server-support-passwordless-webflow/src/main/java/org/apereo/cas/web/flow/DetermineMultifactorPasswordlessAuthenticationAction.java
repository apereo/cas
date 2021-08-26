package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
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
@RequiredArgsConstructor
@Slf4j
public class DetermineMultifactorPasswordlessAuthenticationAction extends AbstractAction {
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final PrincipalFactory passwordlessPrincipalFactory;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val user = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }
        if (multifactorTriggerSelectionStrategy.getMultifactorAuthenticationTriggers().isEmpty()) {
            LOGGER.debug("No multifactor authentication triggers are available or defined");
            return success();
        }

        if (!shouldActivateMultifactorAuthenticationFor(requestContext, user)) {
            LOGGER.debug("User [{}] is not activated to use CAS-provided multifactor authentication providers. "
                + "You may wish to re-examine your CAS configuration to enable and allow for multifactor authentication to be "
                + "combined with passwordless authentication", user);
            return success();
        }

        val attributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects((Map) user.getAttributes());
        val principal = this.passwordlessPrincipalFactory.createPrincipal(user.getName(), attributes);
        val auth = DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .build();
        val service = WebUtils.getService(requestContext);

        val result = resolveMultifactorAuthenticationProvider(requestContext, auth, service);
        if (result.isEmpty()) {
            LOGGER.debug("No CAS-provided multifactor authentication trigger required user [{}] to proceed with MFA. "
                + "CAS will proceed with its normal passwordless authentication flow.", user);
            return success();
        }

        populateContextWithAuthenticationResult(requestContext, auth, service);

        LOGGER.debug("Proceed with multifactor authentication flow [{}] for user [{}]", result.get(), user);
        return new EventFactorySupport().event(this, result.map(MultifactorAuthenticationProvider::getId).orElse(StringUtils.EMPTY));
    }

    /**
     * Populate context with authentication result.
     *
     * @param requestContext the request context
     * @param auth           the auth
     * @param service        the service
     */
    protected void populateContextWithAuthenticationResult(final RequestContext requestContext, final Authentication auth,
                                                           final WebApplicationService service) {
        val builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
        val authenticationResult = builder
            .collect(auth)
            .build(this.authenticationSystemSupport.getPrincipalElectionStrategy(), service);

        WebUtils.putAuthenticationResultBuilder(builder, requestContext);
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(auth, requestContext);
    }

    /**
     * Resolve multifactor authentication provider.
     *
     * @param requestContext the request context
     * @param auth           the auth
     * @param service        the service
     * @return the optional
     */
    protected Optional<MultifactorAuthenticationProvider> resolveMultifactorAuthenticationProvider(final RequestContext requestContext, final Authentication auth,
                                                                                                   final WebApplicationService service) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val registeredService = WebUtils.getRegisteredService(requestContext);
            return multifactorTriggerSelectionStrategy.resolve(request, registeredService, auth, service);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    /**
     * Should activate multifactor authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean shouldActivateMultifactorAuthenticationFor(final RequestContext requestContext,
                                                                 final PasswordlessUserAccount user) {
        val status = user.getMultifactorAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for multifactor authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for multifactor authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isMultifactorAuthenticationActivated();

    }
}
