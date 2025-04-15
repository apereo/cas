package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class InquireInterruptAction extends BaseCasWebflowAction {
    private final List<InterruptInquirer> interruptInquirers;

    private final CasConfigurationProperties casProperties;

    private final InterruptTrackingEngine interruptTrackingEngine;

    private final ObjectProvider<ScriptResourceCacheManager> scriptResourceCacheManager;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        if (WebUtils.isInterruptAuthenticationFlowFinalized(requestContext)) {
            return getInterruptSkippedEvent();
        }
        val authentication = WebUtils.getAuthentication(requestContext);
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);

        val forceInquiry = isInterruptInquiryForcedFor(registeredService);
        if (!forceInquiry && interruptTrackingEngine.isInterrupted(requestContext)) {
            val currentResponse = interruptTrackingEngine.forCurrentRequest(requestContext);
            if (currentResponse.isPresent()) {
                val interruptResponse = inquire(requestContext);
                if (interruptResponse.isPresent() && interruptResponse.get().equals(currentResponse.get())) {
                    LOGGER.debug("Authentication event has already finalized interrupt. Skipping...");
                    return getInterruptSkippedEvent();
                }
            }
        }

        if (registeredService != null && registeredService.getWebflowInterruptPolicy() != null) {
            if (shouldSkipInterruptForPrincipalAttributes(registeredService, authentication)) {
                return getInterruptSkippedEvent();
            }
            if (shouldSkipInterruptForGroovyScript(requestContext, registeredService, authentication)) {
                return getInterruptSkippedEvent();
            }
        }

        return inquire(requestContext)
            .map(interruptResponse -> {
                LOGGER.debug("Interrupt inquiry is required since inquirer produced a response [{}]", interruptResponse);
                InterruptUtils.putInterruptIn(requestContext, interruptResponse);
                InterruptUtils.putInterruptTriggerMode(requestContext, casProperties.getInterrupt().getCore().getTriggerMode());
                WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);
            })
            .orElseGet(() -> {
                LOGGER.debug("Webflow interrupt is skipped since no inquirer produced a response");
                return getInterruptSkippedEvent();
            });
    }

    protected boolean shouldSkipInterruptForGroovyScript(final RequestContext requestContext,
                                                         final WebBasedRegisteredService registeredService,
                                                         final Authentication authentication) throws Throwable {
        val policy = registeredService.getWebflowInterruptPolicy();
        if (StringUtils.isBlank(policy.getGroovyScript())) {
            return false;
        }

        val groovyScript = SpringExpressionLanguageValueResolver.getInstance().resolve(policy.getGroovyScript());
        val script = scriptResourceCacheManager.getObject().resolveScriptableResource(
            groovyScript, registeredService.getServiceId(), registeredService.getName());

        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val args = CollectionUtils.<String, Object>wrap(
            "attributes", attributes,
            "username", authentication.getPrincipal().getId(),
            "registeredService", registeredService,
            "service", WebUtils.getService(requestContext),
            "logger", LOGGER);
        script.setBinding(args);
        return !script.execute(args.values().toArray(), Boolean.class);
    }

    protected boolean shouldSkipInterruptForPrincipalAttributes(final WebBasedRegisteredService registeredService,
                                                                final Authentication authentication) {
        val policy = registeredService.getWebflowInterruptPolicy();
        if (StringUtils.isBlank(policy.getAttributeName()) || StringUtils.isBlank(policy.getAttributeValue())) {
            return false;
        }
        
        val instance = SpringExpressionLanguageValueResolver.getInstance();
        val attributeName = instance.resolve(policy.getAttributeName());
        val attributeValue = instance.resolve(policy.getAttributeValue());

        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val attributeToMatch = RegexUtils.findFirst(attributeName, attributes.keySet());
        LOGGER.trace("Checking attribute [{}] to match [{}] in current set of attributes [{}]",
            attributeToMatch, attributeValue, attributes);
        return attributeToMatch
            .filter(attributes::containsKey)
            .map(attributes::get)
            .flatMap(values -> RegexUtils.findFirst(attributeValue, values))
            .stream()
            .findFirst()
            .isEmpty();
    }

    protected boolean isInterruptInquiryForcedFor(final WebBasedRegisteredService registeredService) {
        return casProperties.getInterrupt().getCore().isForceExecution()
            || (registeredService != null && registeredService.getWebflowInterruptPolicy().getForceExecution().isTrue());
    }

    private Event getInterruptSkippedEvent() {
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
    }

    protected Optional<InterruptResponse> inquire(final RequestContext requestContext) {
        return interruptInquirers
            .stream()
            .map(inquirer -> {
                try {
                    LOGGER.trace("Invoking interrupt inquirer using [{}]", inquirer.getName());
                    val authentication = WebUtils.getAuthentication(requestContext);
                    val service = WebUtils.getService(requestContext);
                    val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);
                    val credential = WebUtils.getCredential(requestContext);
                    return inquirer.inquire(authentication, registeredService, service, credential, requestContext);
                } catch (final Throwable e) {
                    LoggingUtils.error(LOGGER, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .filter(InterruptResponse::isInterrupt)
            .findFirst();
    }

}
