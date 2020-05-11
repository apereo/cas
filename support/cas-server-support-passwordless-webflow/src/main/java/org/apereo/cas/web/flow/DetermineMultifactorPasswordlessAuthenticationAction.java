package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        return new EventFactorySupport().event(this, result.get());
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
        val builder = new DefaultAuthenticationResultBuilder();
        val authenticationResult = builder
            .collect(auth)
            .build(this.authenticationSystemSupport.getPrincipalElectionStrategy(), service);

        WebUtils.putAuthenticationResultBuilder(builder, requestContext);
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(auth, requestContext);
    }

    protected Optional<String> resolveMultifactorAuthenticationProvider(final RequestContext requestContext, final Authentication auth,
                                                                        final WebApplicationService service) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        try {
            return multifactorTriggerSelectionStrategy.resolve(request, registeredService, auth, service);
        } catch (final MultifactorAuthenticationProviderAbsentException e) {
            LOGGER.error(e.getMessage());
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
        return casProperties.getAuthn().getPasswordless().isMultifactorAuthenticationActivated()
            || user.isMultifactorAuthenticationEligible();
    }
}
