package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GoogleAuthenticatorSaveRegistrationAction extends OneTimeTokenAccountSaveRegistrationAction<GoogleAuthenticatorAccount> {
    /**
     * Parameter name indicating token.
     */
    public static final String REQUEST_PARAMETER_TOKEN = "token";

    private final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    public GoogleAuthenticatorSaveRegistrationAction(
        final OneTimeTokenCredentialRepository repository,
        final CasConfigurationProperties casProperties,
        final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator,
        final TenantExtractor tenantExtractor) {
        super(repository, casProperties, tenantExtractor);
        this.validator = validator;
    }

    @Override
    protected boolean validate(final GoogleAuthenticatorAccount account, final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(__ -> {
            val token = requestContext.getRequestParameters().getRequiredInteger(REQUEST_PARAMETER_TOKEN);
            if (validator.isTokenAuthorizedFor(token, account)) {
                LOGGER.debug("Successfully validated token [{}]", token);
                val googleAuthenticatorToken = new GoogleAuthenticatorToken(token, account.getUsername());
                validator.getTokenRepository().store(googleAuthenticatorToken);
                return true;
            }
            LOGGER.warn("Unable to authorize given token [{}] for account [{}]", token, account);
            return false;
        }, e -> false)
        .apply(account);
    }

    @Override
    protected Event getErrorEvent(final RequestContext requestContext) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return error();
    }

    @Override
    protected GoogleAuthenticatorAccount buildOneTimeTokenAccount(final RequestContext requestContext) {
        val acct = super.buildOneTimeTokenAccount(requestContext);
        return GoogleAuthenticatorAccount.from(acct);
    }
}
