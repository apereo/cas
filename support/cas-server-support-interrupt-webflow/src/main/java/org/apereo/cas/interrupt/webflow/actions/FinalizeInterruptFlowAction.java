package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link FinalizeInterruptFlowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class FinalizeInterruptFlowAction extends BaseCasWebflowAction {
    private final InterruptTrackingEngine interruptTrackingEngine;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val response = InterruptUtils.getInterruptFrom(requestContext);
        if (response.isBlock() && !requestContext.getRequestParameters().contains("link")) {
            val registeredService = WebUtils.getRegisteredService(requestContext);
            val accessUrl = Optional.ofNullable(registeredService)
                .map(service -> service.getAccessStrategy().getUnauthorizedRedirectUrl())
                .orElse(null);
            if (accessUrl != null) {
                val url = accessUrl.toURL().toExternalForm();
                val externalContext = requestContext.getExternalContext();
                externalContext.requestExternalRedirect(url);
                externalContext.recordResponseComplete();
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_STOP);
            }
            LOGGER.warn("Interrupt response has blocked the authentication flow");
            throw UnauthorizedServiceException.denied("Rejected");
        }

        if (requestContext.getRequestParameters().contains("link")) {
            val link = requestContext.getRequestParameters().get("link");
            LOGGER.debug("Finalizing interrupt flow with link [{}]", link);
            val validLink = response.getLinks().containsValue(link);
            if (!validLink) {
                LOGGER.warn("Link [{}] is not valid and is not part of the interrupt response", link);
                throw UnauthorizedServiceException.denied("Rejected");
            }
        }
        
        val authentication = WebUtils.getAuthentication(requestContext);
        interruptTrackingEngine.trackInterrupt(requestContext, response);
        WebUtils.putAuthentication(authentication, requestContext);
        WebUtils.putInterruptAuthenticationFlowFinalized(requestContext);
        return success();
    }
}
