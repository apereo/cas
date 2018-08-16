package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class PasswordChangeAction extends AbstractAction {

    /**
     * Password Update Success event.
     */
    public static final String PASSWORD_UPDATE_SUCCESS = "passwordUpdateSuccess";

    private static final String PASSWORD_VALIDATION_FAILURE_CODE = "pm.validationFailure";
    private static final String DEFAULT_MESSAGE = "Could not update the account password";


    private static final MessageBuilder ERROR_MSG_BUILDER = new MessageBuilder().error();

    private final PasswordManagementService passwordManagementService;
    private final PasswordValidationService passwordValidationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            final UsernamePasswordCredential c = (UsernamePasswordCredential) WebUtils.getCredential(requestContext);
            LOGGER.debug("Retrieved the current credential from webflow [{}]", c);
            final PasswordChangeBean bean = requestContext.getFlowScope().get(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, PasswordChangeBean.class);

            LOGGER.debug("Attempting to validate the provided password");
            if (!passwordValidationService.isValid(c, bean)) {
                LOGGER.error("Failed to validate the password; perhaps the provided passwords are blank or the password does not match the expected policy pattern");
                return getErrorEvent(requestContext, PASSWORD_VALIDATION_FAILURE_CODE, DEFAULT_MESSAGE);
            }
            LOGGER.debug("Attempting to update the password");
            if (passwordManagementService.change(c, bean)) {
                WebUtils.putCredential(requestContext, new UsernamePasswordCredential(c.getUsername(), bean.getPassword()));
                return new EventFactorySupport().event(this, PASSWORD_UPDATE_SUCCESS);
            }
            LOGGER.error("Unable to update the password");
        } catch (final InvalidPasswordException e) {
            LOGGER.error(e.getMessage(), e);
            return getErrorEvent(requestContext,
                PASSWORD_VALIDATION_FAILURE_CODE + StringUtils.defaultIfBlank(e.getCode(), ""),
                StringUtils.defaultIfBlank(e.getValidationMessage(), DEFAULT_MESSAGE),
                e.getParams());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return getErrorEvent(requestContext, "pm.updateFailure", DEFAULT_MESSAGE);
    }

    private Event getErrorEvent(final RequestContext ctx, final String code, final String message, final Object... params) {
        ctx.getMessageContext().addMessage(ERROR_MSG_BUILDER.code(code).
            defaultText(message).args(params).build());
        return error();
    }
}
