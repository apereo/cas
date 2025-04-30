package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
public class OneTimeTokenAccountSaveRegistrationAction<T extends OneTimeTokenAccount> extends BaseCasWebflowAction {

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

    private final TenantExtractor tenantExtractor;

    protected OneTimeTokenAccount buildOneTimeTokenAccount(final RequestContext requestContext) {
        val currentAcct = getCandidateAccountFrom(requestContext);
        val accountName = WebUtils.getRequestParameterOrAttribute(requestContext, REQUEST_PARAMETER_ACCOUNT_NAME).orElseThrow();
        return OneTimeTokenAccount.builder()
            .username(currentAcct.getUsername())
            .secretKey(currentAcct.getSecretKey())
            .validationCode(currentAcct.getValidationCode())
            .scratchCodes(currentAcct.getScratchCodes())
            .name(accountName)
            .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
            .build();
    }

    protected T getCandidateAccountFrom(final RequestContext requestContext) {
        return (T) requestContext.getFlowScope()
            .get(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, OneTimeTokenAccount.class);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val currentAcct = getCandidateAccountFrom(requestContext);
            val deviceRegistrationEnabled = MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
            if (!deviceRegistrationEnabled) {
                LOGGER.warn("Device registration is disabled for [{}]", currentAcct.getUsername());
                return getErrorEvent(requestContext);
            }

            if (!isMultipleDeviceRegistrationEnabled(requestContext)
                && repository.count(currentAcct.getUsername()) > 0) {
                LOGGER.warn("Unable to register multiple devices for [{}]", currentAcct.getUsername());
                return getErrorEvent(requestContext);
            }
            val account = (T) buildOneTimeTokenAccount(requestContext);
            if (!validate(account, requestContext)) {
                LOGGER.error("Unable to validate account [{}]", account);
                return getErrorEvent(requestContext);
            }

            val validate = requestContext.getRequestParameters().getBoolean(REQUEST_PARAMETER_VALIDATE);
            if (validate == null || !validate) {
                LOGGER.trace("Storing account [{}]", account);
                MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(requestContext, repository.save(account));
            }
            return success();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return getErrorEvent(requestContext);
    }

    private boolean isMultipleDeviceRegistrationEnabled(final RequestContext requestContext) {
        return tenantExtractor.extract(requestContext)
            .filter(tenantDefinition -> !tenantDefinition.getProperties().isEmpty())
            .map(tenantDefinition -> tenantDefinition.bindProperties().orElseThrow())
            .map(properties -> properties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled())
            .orElseGet(() -> casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled());
    }

    protected boolean validate(final T account, final RequestContext requestContext) {
        return true;
    }

    protected Event getErrorEvent(final RequestContext requestContext) {
        return error();
    }
}
