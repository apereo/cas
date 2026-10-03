package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.authentication.Authentication;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorDeleteAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticatorDeleteAccountAction extends BaseCasWebflowAction {
    /**
     * Account property indicating account removal is now verified.
     */
    public static final String ACCOUNT_PROPERTY_REMOVAL_VERIFIED = "removalVerified";

    private final OneTimeTokenCredentialRepository repository;
    
    private final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    /**
     * Verify or carry out the removal of one of the authenticated user's devices. The account id is a
     * request parameter, so the account is looked up among the authenticated principal's own devices;
     * a request without an authentication, or for a device that belongs to another user, is refused.
     *
     * @param requestContext the request context
     * @return the event
     * @throws Throwable when the removal is not authorized
     */
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val requestParameters = requestContext.getRequestParameters();
        val accountId = NumberUtils.toLong(requestParameters.getRequired(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID));
        val validate = requestParameters.getBoolean(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE);
        val authentication = WebUtils.getAuthentication(requestContext);
        val account = findAccount(authentication, accountId);

        if (BooleanUtils.isTrue(validate)) {
            val token = requestParameters.getRequired(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, String.class);
            val principal = authentication.getPrincipal().getId();
            LOGGER.debug("Validating account [{}] with token [{}] for principal [{}]", accountId, token, principal);
            val tokenCredential = new GoogleAuthenticatorTokenCredential(token, accountId);
            val validatedToken = validator.validate(authentication, tokenCredential);
            if (validatedToken != null) {
                LOGGER.debug("Validated OTP token [{}] successfully for [{}]", validatedToken, principal);
                accountRemovalVerified(requestContext, findAccount(authentication, accountId));
                return success();
            }
            LOGGER.warn("Authorization of OTP token [{}] has failed", token);
            throw new FailedLoginException("Failed to authenticate code " + token);
        }

        if (!isAccountRemovalVerified(requestContext, account)) {
            LOGGER.warn("Account removal is not verified for [{}]", account.getId());
            throw new FailedLoginException("Unauthorized account removal attempt " + account.getId());
        }

        LOGGER.debug("Deleting account [{}]", account.getId());
        repository.delete(account.getId());
        return success();
    }

    /**
     * Find the device among the authenticated user's own devices. An identifier that is not a number
     * arrives here as zero, which no device carries, so it is refused like any device the user does not own.
     * The device is looked up again once the token is validated, since validation may have changed it
     * (a used scratch code, the last-used time), and the verification flag must not undo those changes.
     *
     * @param authentication the authentication, if any
     * @param accountId      the account id
     * @return the account
     * @throws FailedLoginException when the user has no such device
     */
    private OneTimeTokenAccount findAccount(@Nullable final Authentication authentication, final long accountId) throws FailedLoginException {
        return Optional.ofNullable(authentication)
            .map(auth -> repository.get(auth.getPrincipal().getId(), accountId))
            .orElseThrow(() -> new FailedLoginException("Unauthorized account removal attempt " + accountId));
    }

    protected void accountRemovalVerified(final RequestContext requestContext, final OneTimeTokenAccount account) {
        GoogleAuthenticatorAccountVerificationUtils.markVerified(account, ACCOUNT_PROPERTY_REMOVAL_VERIFIED);
        repository.update(account);
    }

    protected boolean isAccountRemovalVerified(final RequestContext requestContext, final OneTimeTokenAccount account) {
        return GoogleAuthenticatorAccountVerificationUtils.isVerified(account, ACCOUNT_PROPERTY_REMOVAL_VERIFIED);
    }
}
