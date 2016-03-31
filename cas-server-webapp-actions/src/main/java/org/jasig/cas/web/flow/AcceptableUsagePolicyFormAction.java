package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Webflow action to receive and record the AUP response.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("acceptableUsagePolicyFormAction")
public class AcceptableUsagePolicyFormAction {

    /** Event id to signal the policy needs to be accepted. **/
    protected static final String EVENT_ID_MUST_ACCEPT = "mustAccept";

    /** Logger instance. **/
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Boolean> policyMap = new ConcurrentHashMap<>();

    /**
     * Verify whether the policy is accepted.
     *
     * @param context the context
     * @param credential the credential
     * @param messageContext the message context
     * @return success if policy is accepted. {@link #EVENT_ID_MUST_ACCEPT} otherwise.
     */
    public Event verify(final RequestContext context, final Credential credential,
                              final MessageContext messageContext)  {
        final String key = credential.getId();
        if (this.policyMap.containsKey(key)) {
            final Boolean hasAcceptedPolicy = this.policyMap.get(key);
            return hasAcceptedPolicy ? success() : accept();
        }
        return accept();
    }

    /**
     * Record the fact that the policy is accepted.
     *
     * @param context the context
     * @param credential the credential
     * @param messageContext the message context
     * @return success if policy acceptance is recorded successfully.
     */
    public Event submit(final RequestContext context, final Credential credential,
                              final MessageContext messageContext)  {
        this.policyMap.put(credential.getId(), Boolean.TRUE);
        return success();
    }

    /**
     * Success event.
     *
     * @return the event
     */
    protected final Event success() {
        return new EventFactorySupport().success(this);
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
