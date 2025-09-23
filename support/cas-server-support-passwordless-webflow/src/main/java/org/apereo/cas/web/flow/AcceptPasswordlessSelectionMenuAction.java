package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptPasswordlessSelectionMenuAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class AcceptPasswordlessSelectionMenuAction extends BasePasswordlessCasWebflowAction {
    protected final PasswordlessUserAccountStore passwordlessUserAccountStore;

    public AcceptPasswordlessSelectionMenuAction(final CasConfigurationProperties casProperties,
                                                 final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                 final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                 final PrincipalFactory passwordlessPrincipalFactory,
                                                 final AuthenticationSystemSupport authenticationSystemSupport) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);

        if (!user.isAllowSelectionMenu()) {
            LOGGER.error("Passwordless account [{}] is not allowed to select options", user.getUsername());
            return buildErrorEvent(requestContext);
        }

        val selection = extractSelectedAuthenticationOption(requestContext);
        if (selection == PasswordlessSelectionMenu.PASSWORD && !doesPasswordlessAccountRequestPassword(user)) {
            LOGGER.warn("Passwordless account [{}] does not require a password", user.getUsername());
            return buildErrorEvent(requestContext);
        }
        if (selection == PasswordlessSelectionMenu.DELEGATION && !isDelegatedAuthenticationActiveFor(requestContext, user)) {
            LOGGER.warn("Passwordless account [{}] does not allow delegated authentication", user.getUsername());
            return buildErrorEvent(requestContext);
        }
        if (selection == PasswordlessSelectionMenu.MFA && !shouldActivateMultifactorAuthenticationFor(requestContext, user)) {
            LOGGER.warn("Passwordless account [{}] does not allow multifactor authentication", user.getUsername());
            return buildErrorEvent(requestContext);
        }
        return buildFinalSelectionEvent(requestContext, selection);
    }

    protected Event buildFinalSelectionEvent(final RequestContext requestContext, final PasswordlessSelectionMenu selection) {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val finalEvent = switch (selection) {
            case PASSWORD -> {
                WebUtils.putCasLoginFormViewable(requestContext, doesPasswordlessAccountRequestPassword(user));
                yield CasWebflowConstants.TRANSITION_ID_PROMPT;
            }
            case TOKEN -> CasWebflowConstants.TRANSITION_ID_DISPLAY;
            case MFA -> CasWebflowConstants.TRANSITION_ID_MFA;
            case DELEGATION -> CasWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_REDIRECT;
        };

        return eventFactory.event(this, finalEvent);
    }

    protected PasswordlessSelectionMenu extractSelectedAuthenticationOption(final RequestContext requestContext) {
        return requestContext.getRequestParameters().getRequired("selection", PasswordlessSelectionMenu.class);
    }

    protected Event buildErrorEvent(final RequestContext requestContext) {
        return error();
    }

    public enum PasswordlessSelectionMenu {
        /**
         * Password selection menu.
         */
        PASSWORD,
        /**
         * Token selection menu.
         */
        TOKEN,
        /**
         * Mfa selection menu.
         */
        MFA,
        /**
         * Delegation selection menu.
         */
        DELEGATION
    }
}
