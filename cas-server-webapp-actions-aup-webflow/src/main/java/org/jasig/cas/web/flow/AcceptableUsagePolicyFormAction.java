package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the AUP response.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("acceptableUsagePolicyFormAction")
public class AcceptableUsagePolicyFormAction extends AbstractAction {

    /**
     * Event id to signal the policy needs to be accepted.
     **/
    private static final String EVENT_ID_MUST_ACCEPT = "mustAccept";

    /**
     * Logger instance.
     **/
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository repository;

    /**
     * Verify whether the policy is accepted.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if policy is accepted. {@link #EVENT_ID_MUST_ACCEPT} otherwise.
     */
    public Event verify(final RequestContext context, final Credential credential,
                        final MessageContext messageContext) {
        if (repository.verify(context, credential)) {
            return success();
        }
        return accept();
    }

    /**
     * Record the fact that the policy is accepted.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if policy acceptance is recorded successfully.
     */
    public Event submit(final RequestContext context, final Credential credential,
                          final MessageContext messageContext) {
        if (repository.submit(context, credential)) {
            return success();
        }

        return error();
    }


    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return verify(requestContext, WebUtils.getCredential(requestContext), requestContext.getMessageContext());
    }

    /**
     * Accept event signaled by id {@link #EVENT_ID_MUST_ACCEPT}.
     *
     * @return the event
     */
    protected final Event accept() {
        return new EventFactorySupport().event(this, EVENT_ID_MUST_ACCEPT);
    }
}
