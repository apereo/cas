package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DetermineMultifactorPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class DetermineMultifactorPasswordlessAuthenticationAction extends BasePasswordlessCasWebflowAction {

    public DetermineMultifactorPasswordlessAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
        final PrincipalFactory passwordlessPrincipalFactory,
        final AuthenticationSystemSupport authenticationSystemSupport) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (user == null) {
            LOGGER.error("Unable to locate passwordless account in the flow");
            return error();
        }
        if (!user.hasContactInformation()) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.invalid.user");
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
        return eventFactory.event(this, result.map(MultifactorAuthenticationProvider::getId).orElse(StringUtils.EMPTY));
    }
}
