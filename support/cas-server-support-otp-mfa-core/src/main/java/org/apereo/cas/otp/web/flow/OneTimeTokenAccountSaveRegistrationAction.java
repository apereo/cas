package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

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

    /**
     * Parameter name indicating account name.
     */
    public static final String REQUEST_PARAMETER_ACCOUNT_NAME = "accountName";

    private final OneTimeTokenCredentialRepository repository;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val currentAcct = requestContext.getFlowScope()
                .get(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);

            if (!casProperties.getAuthn().getMfa().getGauth().isMultipleDeviceRegistrationEnabled()) {
                if (repository.count(currentAcct.getUsername()) > 0) {
                    LOGGER.warn("Unable to register multiple devices for [{}]", currentAcct.getUsername());
                    return error();
                }
            }
            
            val acctName = requestContext.getRequestParameters().getRequired(REQUEST_PARAMETER_ACCOUNT_NAME);
            val account = OneTimeTokenAccount.builder()
                .username(currentAcct.getUsername())
                .secretKey(currentAcct.getSecretKey())
                .validationCode(currentAcct.getValidationCode())
                .scratchCodes(currentAcct.getScratchCodes())
                .name(acctName)
                .build();
            LOGGER.trace("Storing account [{}]", account);
            WebUtils.putOneTimeTokenAccount(requestContext, repository.save(account));
            return success();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return error();
    }
}
