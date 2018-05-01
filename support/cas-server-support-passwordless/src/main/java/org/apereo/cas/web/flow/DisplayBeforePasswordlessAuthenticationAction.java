package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.PasswordlessAuthenticationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DisplayBeforePasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DisplayBeforePasswordlessAuthenticationAction extends AbstractAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;
    private final CommunicationsManager communicationsManager;
    private final PasswordlessAuthenticationProperties passwordlessProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final AttributeMap<Object> attributes = requestContext.getCurrentEvent().getAttributes();
        if (attributes.contains("error")) {
            final Exception e = attributes.get(CasWebflowConstants.TRANSITION_ID_ERROR, Exception.class);
            requestContext.getFlowScope().put(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            final PasswordlessUserAccount user = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
            WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            return success();
        }
        final String username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        final Optional<PasswordlessUserAccount> account = passwordlessUserAccountStore.findUser(username);
        if (account.isPresent()) {
            final PasswordlessUserAccount user = account.get();
            WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            final String token = passwordlessTokenRepository.createToken(user.getUsername());

            communicationsManager.validate();
            if (communicationsManager.isMailSenderDefined() && StringUtils.isNotBlank(user.getEmail())) {
                communicationsManager.email(token,
                    passwordlessProperties.getTokens().getMail().getFrom(),
                    passwordlessProperties.getTokens().getMail().getSubject(),
                    user.getEmail());
            }
            if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
                communicationsManager.sms(passwordlessProperties.getTokens().getMail().getFrom(), user.getPhone(), token);
            }

            passwordlessTokenRepository.deleteTokens(user.getUsername());
            passwordlessTokenRepository.saveToken(user.getUsername(), token);
            return success();
        }
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
    }
}
