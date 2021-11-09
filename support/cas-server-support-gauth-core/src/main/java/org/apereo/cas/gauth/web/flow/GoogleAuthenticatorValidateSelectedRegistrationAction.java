package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorValidateSelectedRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GoogleAuthenticatorValidateSelectedRegistrationAction extends AbstractAction {
    private static final String CODE = "screen.authentication.gauth.invalid";

    private static void addErrorMessageToContext(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, CODE);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val account = WebUtils.getOneTimeTokenAccount(requestContext, OneTimeTokenAccount.class);
        if (account == null) {
            LOGGER.warn("Unable to determine google authenticator account");
            addErrorMessageToContext(requestContext);
            return error();
        }
        val credential = WebUtils.getCredential(requestContext, GoogleAuthenticatorTokenCredential.class);
        if (credential == null) {
            LOGGER.warn("Unable to determine google authenticator token credential");
            addErrorMessageToContext(requestContext);
            return error();
        }
        LOGGER.trace("Located account [{}] to be used for credential [{}]", account, credential);
        if (credential.getAccountId() == null || credential.getAccountId() != account.getId()) {
            LOGGER.warn("Google authenticator token credential is not assigned a valid account id");
            addErrorMessageToContext(requestContext);
            return error();
        }
        return null;
    }
}
