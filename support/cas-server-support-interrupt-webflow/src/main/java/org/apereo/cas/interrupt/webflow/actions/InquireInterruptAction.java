package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class InquireInterruptAction extends AbstractAction {
    /**
     * Attribute recorded in authentication to indicate interrupt is finalized.
     */
    public static final String AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT = "finalizedInterrupt";

    private final List<InterruptInquirer> interruptInquirers;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val service = WebUtils.getService(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val credential = WebUtils.getCredential(requestContext);
        val eventFactorySupport = new EventFactorySupport();

        if (authentication.getAttributes().containsKey(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT)) {
            LOGGER.debug("Authentication event has already finalized interrupt. Skipping...");
            return getInterruptSkippedEvent();
        }
        for (val inquirer : this.interruptInquirers) {
            LOGGER.debug("Invoking interrupt inquirer using [{}]", inquirer.getName());
            val response = inquirer.inquire(authentication, registeredService, service, credential, requestContext);
            if (response != null && response.isInterrupt()) {
                LOGGER.debug("Interrupt inquiry is required since inquirer produced a response [{}]", response);
                InterruptUtils.putInterruptIn(requestContext, response);
                WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);
            }
        }
        LOGGER.debug("Webflow interrupt is skipped since no inquirer produced a response");
        return getInterruptSkippedEvent();
    }

    private Event getInterruptSkippedEvent() {
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }
}
