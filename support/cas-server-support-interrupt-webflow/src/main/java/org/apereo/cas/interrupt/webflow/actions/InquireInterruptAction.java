package org.apereo.cas.interrupt.webflow.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class InquireInterruptAction extends AbstractAction {
    private final InterruptInquirer interruptInquirer;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var authentication = WebUtils.getAuthentication(requestContext);
        final Service service = WebUtils.getService(requestContext);
        final var registeredService = WebUtils.getRegisteredService(requestContext);
        final var credential = WebUtils.getCredential(requestContext);

        final var response = this.interruptInquirer.inquire(authentication, registeredService, service, credential);
        if (response == null || !response.isInterrupt()) {
            return no();
        }
        InterruptUtils.putInterruptIn(requestContext, response);
        WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
        return yes();
    }
}
