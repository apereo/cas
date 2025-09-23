package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import javax.security.auth.login.FailedLoginException;
import java.util.Objects;

/**
 * This is {@link GoogleAuthenticatorConfirmAccountRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorConfirmAccountRegistrationAction extends BaseCasWebflowAction {
    /**
     * Account property indicating account registration is now verified.
     */
    public static final String ACCOUNT_PROPERTY_REGISTRATION_VERIFIED = "registrationVerified";

    
    private final OneTimeTokenCredentialRepository repository;
    private final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val requestParameters = requestContext.getRequestParameters();
        val accountId = requestParameters.getRequired(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, Long.class);
        val validate = requestParameters.getBoolean(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE);
        val account = repository.get(accountId);
        Objects.requireNonNull(account, "Account cannot be null");
        if (BooleanUtils.isTrue(validate)) {
            val token = requestParameters.getRequired(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, String.class);
            val authentication = WebUtils.getAuthentication(requestContext);
            val principal = authentication.getPrincipal().getId();
            LOGGER.debug("Validating account [{}] with token [{}] for principal [{}]", accountId, token, principal);
            val tokenCredential = new GoogleAuthenticatorTokenCredential(token, accountId);
            val validatedToken = validator.validate(authentication, tokenCredential);
            if (validatedToken != null) {
                LOGGER.debug("Validated OTP token [{}] successfully for [{}]", validatedToken, principal);
                accountRegistrationVerified(requestContext, account);
                return success();
            }
            LOGGER.warn("Authorization of OTP token [{}] has failed", token);
            throw new FailedLoginException("Failed to authenticate code " + token);
        }

        if (!isAccountRegistrationVerified(requestContext, account)) {
            LOGGER.warn("Account registration is not verified for [{}]", account.getId());
            throw new FailedLoginException("Unauthorized account registration attempt for id " + account.getId());
        }

        accountRegistrationUnverified(requestContext, account);
        return success();
    }

    protected void accountRegistrationVerified(final RequestContext requestContext, final OneTimeTokenAccount account) {
        account.getProperties().add(ACCOUNT_PROPERTY_REGISTRATION_VERIFIED);
        repository.update(account);
    }

    protected void accountRegistrationUnverified(final RequestContext requestContext, final OneTimeTokenAccount account) {
        account.getProperties().remove(ACCOUNT_PROPERTY_REGISTRATION_VERIFIED);
        repository.update(account);
    }

    protected boolean isAccountRegistrationVerified(final RequestContext requestContext, final OneTimeTokenAccount account) {
        return account.getProperties().contains(ACCOUNT_PROPERTY_REGISTRATION_VERIFIED);
    }
}
