package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordChangeAction extends AbstractAction {

    /**
     * Password Update Success event.
     */
    public static final String PASSWORD_UPDATE_SUCCESS = "passwordUpdateSuccess";

    private static final String PASSWORD_VALIDATION_FAILURE_CODE = "pm.validationFailure";
    private static final String DEFAULT_MESSAGE = "Could not update the account password";

    private final PasswordManagementService passwordManagementService;
    private final PasswordValidationService passwordValidationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val c = Objects.requireNonNull(WebUtils.getCredential(requestContext, UsernamePasswordCredential.class));
            val bean = getPasswordChangeRequest(requestContext, c);

            LOGGER.debug("Attempting to validate the password change bean for username [{}]", c.getUsername());
            if (!passwordValidationService.isValid(c, bean)) {
                LOGGER.error("Failed to validate the provided password");
                return getErrorEvent(requestContext, PASSWORD_VALIDATION_FAILURE_CODE, DEFAULT_MESSAGE);
            }
            if (passwordManagementService.change(c, bean)) {
                WebUtils.putCredential(requestContext, new UsernamePasswordCredential(c.getUsername(), bean.getPassword()));
                return new EventFactorySupport().event(this, PASSWORD_UPDATE_SUCCESS);
            }
        } catch (final InvalidPasswordException e) {
            return getErrorEvent(requestContext,
                PASSWORD_VALIDATION_FAILURE_CODE + StringUtils.defaultIfBlank(e.getCode(), StringUtils.EMPTY),
                StringUtils.defaultIfBlank(e.getValidationMessage(), DEFAULT_MESSAGE),
                e.getParams());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return getErrorEvent(requestContext, "pm.updateFailure", DEFAULT_MESSAGE);
    }

    /**
     * Gets password change request.
     *
     * @param requestContext the request context
     * @param c              the c
     * @return the password change request
     */
    protected PasswordChangeRequest getPasswordChangeRequest(final RequestContext requestContext, final UsernamePasswordCredential c) {
        val bean = requestContext.getFlowScope().get(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);
        bean.setUsername(c.getUsername());
        return bean;
    }

    private Event getErrorEvent(final RequestContext ctx, final String code, final String message, final Object... params) {
        ctx.getMessageContext().addMessage(new MessageBuilder().error().code(code).defaultText(message).args(params).build());
        return error();
    }
}
