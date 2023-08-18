package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class InquireInterruptAction extends BaseCasWebflowAction {
    /**
     * Attribute recorded in authentication to indicate interrupt is finalized.
     */
    public static final String AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT = "finalizedInterrupt";

    private final List<InterruptInquirer> interruptInquirers;

    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder casCookieBuilder;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        if (WebUtils.isInterruptAuthenticationFlowFinalized(requestContext)) {
            WebUtils.removeInterruptAuthenticationFlowFinalized(requestContext);
            return getInterruptSkippedEvent();
        }

        val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        val service = WebUtils.getService(requestContext);
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);
        val credential = WebUtils.getCredential(requestContext);
        val eventFactorySupport = new EventFactorySupport();

        val forceInquiry = casProperties.getInterrupt().getCore().isForceExecution()
            || (registeredService != null && registeredService.getWebflowInterruptPolicy().getForceExecution().isTrue());
        if (!forceInquiry && isAuthenticationFlowInterruptedAlready(authentication, httpRequest)) {
            LOGGER.debug("Authentication event has already finalized interrupt. Skipping...");
            return getInterruptSkippedEvent();
        }

        if (registeredService != null) {
            val policy = registeredService.getWebflowInterruptPolicy();
            if (policy != null && StringUtils.isNotBlank(policy.getAttributeName()) && StringUtils.isNotBlank(policy.getAttributeValue())) {
                val instance = SpringExpressionLanguageValueResolver.getInstance();
                val attributeName = instance.resolve(policy.getAttributeName());
                val attributeValue = instance.resolve(policy.getAttributeValue());

                val attributes = new HashMap<>(authentication.getAttributes());
                attributes.putAll(authentication.getPrincipal().getAttributes());
                
                val skipInterrupt = RegexUtils.findFirst(attributeName, attributes.keySet())
                    .filter(attributes::containsKey)
                    .map(attributes::get)
                    .flatMap(values -> RegexUtils.findFirst(attributeValue, values))
                    .stream()
                    .findFirst()
                    .isEmpty();
                if (skipInterrupt) {
                    return getInterruptSkippedEvent();
                }
            }
        }

        for (val inquirer : this.interruptInquirers) {
            LOGGER.debug("Invoking interrupt inquirer using [{}]", inquirer.getName());
            val response = FunctionUtils.doUnchecked(() -> inquirer.inquire(authentication, registeredService, service, credential, requestContext));
            if (response != null && response.isInterrupt()) {
                LOGGER.debug("Interrupt inquiry is required since inquirer produced a response [{}]", response);
                InterruptUtils.putInterruptIn(requestContext, response);
                InterruptUtils.putInterruptTriggerMode(requestContext, casProperties.getInterrupt().getCore().getTriggerMode());
                WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);
            }
        }
        LOGGER.debug("Webflow interrupt is skipped since no inquirer produced a response");
        return getInterruptSkippedEvent();
    }

    /**
     * An authentication attempt can only contain {@link #AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT}
     * if the attribute was added to the authentication object prior to creating the SSO session.
     * If interrupt checking is set to execute after SSO sessions, then this attribute cannot be collected.
     *
     * @param authentication authentication attempt
     * @param request        http request
     * @return true/false
     */
    private boolean isAuthenticationFlowInterruptedAlready(final Authentication authentication,
                                                           final HttpServletRequest request) {
        val interrupted = casCookieBuilder.retrieveCookieValue(request);
        return BooleanUtils.toBoolean(interrupted)
            || authentication.getAttributes().containsKey(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT);
    }

    private Event getInterruptSkippedEvent() {
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }
}
