package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.validator.OneTimeTokenAccountValidator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
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
    private final OneTimeTokenAccountValidator tokenValidator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val account = requestContext.getFlowScope()
            .get(OneTimeTokenAccountCheckRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);
        val uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val credential = WebUtils.getCredential(requestContext, OneTimeTokenCredential.class);
        val account = requestContext.getFlowScope().get("key", OneTimeTokenAccount.class);

        int token;
        try {
            token = tokenValidator.parseToken(credential);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unable to extract token from Credential [{}] for user [{}]",
                    credential, uid);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
        }

        LOGGER.debug("Attempting to validate OTP token [{}] with [{}]", token, account);
        val isCodeValid = tokenValidator.isValid(account, token);

        if (isCodeValid) {
            LOGGER.debug("Validated OTP token [{}] to register user [{}]", token, uid);
            repository.save(uid, account.getSecretKey(), account.getValidationCode(), account.getScratchCodes());
            return success();
        }
        if (account.getScratchCodes().contains(token)) {
            LOGGER.warn("User [{}] attempted to use scratch code during OTP registration; this is likely a mistake.", uid);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
        }

        LOGGER.warn("Failed to validate token [{}] to register user [{}]", token, uid);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
