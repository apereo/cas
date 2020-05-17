package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.validator.OneTimeTokenAccountValidator;
import org.apereo.cas.web.support.WebUtils;

//import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Save the registration credential into the repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OneTimeTokenAccountSaveRegistrationAction extends AbstractAction {
    private final OneTimeTokenCredentialRepository repository;
    //private final IGoogleAuthenticator googleAuthenticatorInstance;
    private final OneTimeTokenAccountValidator tokenValidator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val account = requestContext.getFlowScope()
            .get(OneTimeTokenAccountCheckRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);
        val uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val credential = WebUtils.getCredential(requestContext, OneTimeTokenCredential.class);

        if (!StringUtils.isNumeric(credential.getToken())) {
            LOGGER.error("Non-numeric OTP token provided: [{}]", credential.getToken());
            return error();
        }

        val token = Integer.parseInt(credential.getToken());

        LOGGER.debug("Attempting to validate OTP token [{}] with [{}]", token, account);
        //val isCodeValid = this.googleAuthenticatorInstance.authorize(account.getSecretKey(), token);
        val isCodeValid = this.tokenValidator.isValid(account, token);

        if (!isCodeValid && account.getScratchCodes().contains(token)) {
            LOGGER.warn("User [{}] attempted to use scratch code during OTP registration; this is likely a mistake.", uid);
            return error();
        }
        if (isCodeValid) {
            LOGGER.debug("Validated OTP token [{}] to register user [{}]", token, uid);
            repository.save(uid, account.getSecretKey(), account.getValidationCode(), account.getScratchCodes());
            return success();
        }

        LOGGER.warn("Failed to validate token [{}] to register user [{}]", token, uid);
        return error();
    }
}
