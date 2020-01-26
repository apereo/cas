package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.PasswordlessAuthenticationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DisplayBeforePasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DisplayBeforePasswordlessAuthenticationAction extends AbstractAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    private final CommunicationsManager communicationsManager;

    private final PasswordlessAuthenticationProperties passwordlessProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();
        if (attributes.contains(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            val e = attributes.get("error", Exception.class);
            requestContext.getFlowScope().put(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            val user = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
            WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            return success();
        }
        val username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            LOGGER.error("Unable to locate passwordless user account for [{}]", username);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        val user = account.get();
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        val token = passwordlessTokenRepository.createToken(user.getUsername());

        communicationsManager.validate();
        if (communicationsManager.isMailSenderDefined() && StringUtils.isNotBlank(user.getEmail())) {
            val mail = passwordlessProperties.getTokens().getMail();
            communicationsManager.email(mail, user.getEmail(), mail.getFormattedBody(token));
        }
        if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
            communicationsManager.sms(passwordlessProperties.getTokens().getSms().getFrom(), user.getPhone(), token);
        }

        passwordlessTokenRepository.deleteTokens(user.getUsername());
        passwordlessTokenRepository.saveToken(user.getUsername(), token);
        return success();
    }
}
