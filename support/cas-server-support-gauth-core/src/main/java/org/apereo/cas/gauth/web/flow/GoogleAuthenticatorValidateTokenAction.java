package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import javax.security.auth.login.FailedLoginException;

/**
 * This is {@link GoogleAuthenticatorValidateTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticatorValidateTokenAction extends AbstractMultifactorAuthenticationAction {
    protected final CasConfigurationProperties casProperties;
    protected final OneTimeTokenCredentialRepository repository;
    protected final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val token = requestContext.getRequestParameters().getRequired(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, String.class);
        val accountId = requestContext.getRequestParameters().getRequired(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, Long.class);

        val authentication = WebUtils.getAuthentication(requestContext);
        val tokenCredential = new GoogleAuthenticatorTokenCredential(token, accountId);
        val validatedToken = validator.validate(authentication, tokenCredential);
        if (validatedToken != null) {
            val principal = authentication.getPrincipal().getId();
            LOGGER.debug("Validated OTP token [{}] successfully for [{}]", validatedToken, principal);
            val validate = requestContext.getRequestParameters().getBoolean(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE);
            if (validate == null || !validate) {
                validator.store(validatedToken);
            }
            return success();
        }
        LOGGER.warn("Authorization of OTP token [{}] has failed for [{}]", token, authentication.getPrincipal().getId());
        throw new FailedLoginException("Failed to authenticate code " + token);
    }
}
