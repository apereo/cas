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
    
    /**
     * Parameter name indicating a validation request event.
     */
    public static final String REQUEST_PARAMETER_VALIDATE = "validate";

    private final OneTimeTokenCredentialRepository repository;

    private final CasConfigurationProperties casProperties;

    /**
     * Build one time token account.
     *
     * @param requestContext the request context
     * @return the one time token account
     */
    protected OneTimeTokenAccount buildOneTimeTokenAccount(final RequestContext requestContext) {
        val currentAcct = getCandidateAccountFrom(requestContext);
        val accountName = requestContext.getRequestParameters().getRequired(REQUEST_PARAMETER_ACCOUNT_NAME);
        return OneTimeTokenAccount.builder()
            .username(currentAcct.getUsername())
            .secretKey(currentAcct.getSecretKey())
            .validationCode(currentAcct.getValidationCode())
            .scratchCodes(currentAcct.getScratchCodes())
            .name(accountName)
            .build();
    }

    /**
     * Gets candidate account from.
     *
     * @param requestContext the request context
     * @return the candidate account from
     */
    protected OneTimeTokenAccount getCandidateAccountFrom(final RequestContext requestContext) {
        return requestContext.getFlowScope()
            .get(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val currentAcct = getCandidateAccountFrom(requestContext);
            if (!casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled()) {
                if (repository.count(currentAcct.getUsername()) > 0) {
                    LOGGER.warn("Unable to register multiple devices for [{}]", currentAcct.getUsername());
                    return getErrorEvent(requestContext);
                }
            }
            val account = buildOneTimeTokenAccount(requestContext);
            if (!validate(account, requestContext)) {
                LOGGER.error("Unable to validate account");
                return getErrorEvent(requestContext);
            }

            val validate = requestContext.getRequestParameters().getBoolean(REQUEST_PARAMETER_VALIDATE);
            if (validate == null || !validate) {
                LOGGER.trace("Storing account [{}]", account);
                WebUtils.putOneTimeTokenAccount(requestContext, repository.save(account));
            }
            return success();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return getErrorEvent(requestContext);
    }

    /**
     * Validate account and context.
     *
     * @param account        the account
     * @param requestContext the request context
     * @return true/false
     */
    protected boolean validate(final OneTimeTokenAccount account, final RequestContext requestContext) {
        return true;
    }

    /**
     * Gets error event.
     *
     * @param requestContext the request context
     * @return the error event
     */
    protected Event getErrorEvent(final RequestContext requestContext) {
        return error();
    }
}
