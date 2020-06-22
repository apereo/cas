package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Save the registration credential into the repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OneTimeTokenAccountSaveRegistrationAction extends AbstractAction {
    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val otpAcct = requestContext.getFlowScope()
                .get(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);
            val acctName = requestContext.getRequestParameters().getRequired("accountName");
            val account = OneTimeTokenAccount.builder()
                .username(otpAcct.getUsername())
                .secretKey(otpAcct.getSecretKey())
                .validationCode(otpAcct.getValidationCode())
                .scratchCodes(otpAcct.getScratchCodes())
                .name(acctName)
                .build();
            repository.save(account);
            return success();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return error();
    }
}
