package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.PasswordChangeBean;
import org.apereo.cas.web.PasswordChangeService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangeAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private PasswordChangeService passwordChangeService;

    public PasswordChangeAction(final PasswordChangeService passwordChangeService) {
        this.passwordChangeService = passwordChangeService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        try {
            final UsernamePasswordCredential c = (UsernamePasswordCredential) WebUtils.getCredential(requestContext);
            final PasswordChangeBean bean = requestContext.getFlowScope().get("password", PasswordChangeBean.class);
            if (passwordChangeService.execute(c, bean)) {
                return new EventFactorySupport().event(this, "passwordUpdateSuccess");
            }
        } catch (final Exception e) {
            LOGGER.error("Update failed", e.getMessage());
        }
        requestContext.getMessageContext().addMessage(new MessageBuilder().error().code("pm.updateFailure").
                defaultText("Could not update the account password").build());
        return error();
    }
}
