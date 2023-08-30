package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class PasswordChangeAction extends BaseCasWebflowAction {

    private static final String PASSWORD_VALIDATION_FAILURE_CODE = "pm.validationFailure";

    private static final String DEFAULT_MESSAGE = "Could not update the account password";

    private final PasswordManagementService passwordManagementService;

    private final PasswordValidationService passwordValidationService;

    /**
     * Gets password change request.
     *
     * @param requestContext the request context
     * @return the password change request
     */
    protected static PasswordChangeRequest getPasswordChangeRequest(final RequestContext requestContext) {
        val bean = requestContext.getFlowScope().get(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);
        bean.setUsername(PasswordManagementWebflowUtils.getPasswordResetUsername(requestContext));
        return bean;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val bean = getPasswordChangeRequest(requestContext);
            Optional.ofNullable(WebUtils.getCredential(requestContext, UsernamePasswordCredential.class))
                    .ifPresent(credential -> bean.setCurrentPassword(credential.getPassword()));
            
            LOGGER.debug("Attempting to validate the password change bean for username [{}]", bean.getUsername());
            if (StringUtils.isBlank(bean.getUsername()) || !passwordValidationService.isValid(bean)) {
                LOGGER.error("Failed to validate the provided password");
                return getErrorEvent(requestContext, PASSWORD_VALIDATION_FAILURE_CODE, DEFAULT_MESSAGE);
            }
            if (passwordManagementService.change(bean)) {
                val credential = new UsernamePasswordCredential(bean.getUsername(), bean.toPassword());
                WebUtils.putCredential(requestContext, credential);
                LOGGER.info("Password successfully changed for [{}]", bean.getUsername());
                return getSuccessEvent(requestContext, bean);
            }
        } catch (final InvalidPasswordException e) {
            return getErrorEvent(requestContext,
                PASSWORD_VALIDATION_FAILURE_CODE + StringUtils.defaultIfBlank(e.getCode(), StringUtils.EMPTY),
                StringUtils.defaultIfBlank(e.getValidationMessage(), DEFAULT_MESSAGE),
                e.getParams());
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return getErrorEvent(requestContext, "pm.updateFailure", DEFAULT_MESSAGE);
    }

    /**
     * Finalize password change success.
     *
     * @param requestContext the request context
     * @param bean           the bean
     * @return the event
     */
    protected Event getSuccessEvent(final RequestContext requestContext,
                                    final PasswordChangeRequest bean) {
        return new EventFactorySupport()
            .event(this, CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS,
                new LocalAttributeMap<>("passwordChangeRequest", bean));
    }

    private Event getErrorEvent(final RequestContext ctx, final String code, final String message, final Object... params) {
        WebUtils.addErrorMessageToContext(ctx, code, message, params);
        return error();
    }
}
