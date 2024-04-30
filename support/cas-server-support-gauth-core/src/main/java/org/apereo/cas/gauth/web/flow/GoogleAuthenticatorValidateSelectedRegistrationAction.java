package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorValidateSelectedRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GoogleAuthenticatorValidateSelectedRegistrationAction extends BaseCasWebflowAction {
    private static final String CODE = "screen.authentication.gauth.invalid";

    private static void addErrorMessageToContext(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, CODE);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(requestContext)) {
            val trustedDevice = MultifactorAuthenticationTrustUtils.getMultifactorAuthenticationTrustRecord(requestContext, MultifactorAuthenticationTrustRecord.class).orElseThrow();
            LOGGER.info("Multifactor authentication device [{}] is trusted with fingerprint [{}]", trustedDevice.getName(), trustedDevice.getDeviceFingerprint());
            return success(trustedDevice);
        }

        val account = MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccount(requestContext, OneTimeTokenAccount.class);
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
