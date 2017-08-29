package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PasswordChangeAction extends AbstractAction {

    /**
     * Password Update Success event.
     */
    public static final String PASSWORD_UPDATE_SUCCESS = "passwordUpdateSuccess";

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangeAction.class);
    private static final MessageBuilder ERROR_MSG_BUILDER = new MessageBuilder().error();

    private final PasswordManagementService passwordManagementService;
    private final PasswordValidationService passwordValidationService;

    public PasswordChangeAction(final PasswordManagementService passwordManagementService,
                                final PasswordValidationService passwordValidationService) {
        this.passwordManagementService = passwordManagementService;
        this.passwordValidationService = passwordValidationService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        try {
            final UsernamePasswordCredential c = (UsernamePasswordCredential) WebUtils.getCredential(requestContext);
            final PasswordChangeBean bean = requestContext.getFlowScope()
                    .get(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, PasswordChangeBean.class);

            if (!passwordValidationService.isValid(c, bean)) {
                return getErrorEvent(requestContext, "pm.validationFailure");
            }
            if (passwordManagementService.change(c, bean)) {
                return new EventFactorySupport().event(this, PASSWORD_UPDATE_SUCCESS);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return getErrorEvent(requestContext, "pm.updateFailure");
    }

    private Event getErrorEvent(final RequestContext ctx, final String code) {
        ctx.getMessageContext().addMessage(ERROR_MSG_BUILDER.code(code).
                defaultText("Could not update the account password").build());
        return error();
    }
}
