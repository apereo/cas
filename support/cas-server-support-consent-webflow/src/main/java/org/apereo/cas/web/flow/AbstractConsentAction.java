package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.CasConsentableAttribute;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentReminderOptions;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractConsentAction extends BaseCasWebflowAction {
    /**
     * CAS Settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Service selection strategies.
     */
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * The consent engine that handles calculations.
     */
    protected final ConsentEngine consentEngine;

    /**
     * The attribute definition store
     * that might contain metadata about consentable attributes.
     */
    protected final AttributeDefinitionStore attributeDefinitionStore;

    /**
     * The application context.
     */
    protected final ConfigurableApplicationContext applicationContext;

    /**
     * Gets registered service for consent.
     *
     * @param requestContext the request context
     * @param service        the service
     * @return the registered service for consent
     */
    protected RegisteredService getRegisteredServiceForConsent(final RequestContext requestContext, final Service service) {
        return FunctionUtils.doUnchecked(() -> {
            val serviceToUse = authenticationRequestServiceSelectionStrategies.resolveService(service);
            val registeredService = this.servicesManager.findServiceBy(serviceToUse);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            return registeredService;
        });
    }

    protected void prepareConsentForRequestContext(final RequestContext requestContext) throws Throwable {
        val consentProperties = casProperties.getConsent().getCore();

        val originalService = WebUtils.getService(requestContext);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(originalService);
        val registeredService = getRegisteredServiceForConsent(requestContext, service);
        val authentication = WebUtils.getAuthentication(requestContext);
        val attributes = consentEngine.resolveConsentableAttributesFrom(authentication, service, registeredService);
        val flowScope = requestContext.getFlowScope();

        prepareConsentableAttributes(attributes, requestContext);

        WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
        WebUtils.putServiceIntoFlashScope(requestContext, service);

        val decision = consentEngine.findConsentDecision(service, registeredService, authentication);
        flowScope.put("option", Optional.ofNullable(decision)
            .map(consentDecision -> consentDecision.getOptions().getValue())
            .orElseGet(ConsentReminderOptions.ATTRIBUTE_NAME::getValue));

        val reminder = decision == null ? consentProperties.getReminder() : decision.getReminder();
        flowScope.put("reminder", reminder);
        flowScope.put("reminderTimeUnit", Optional.ofNullable(decision)
            .map(consentDecision -> consentDecision.getReminderTimeUnit().name())
            .orElseGet(() -> consentProperties.getReminderTimeUnit().name()));
    }

    /**
     * Prepare consentable attributes.
     *
     * @param attributes the attributes
     * @param context    the context
     */
    protected void prepareConsentableAttributes(final Map<String, List<Object>> attributes, final RequestContext context) {
        val builders = applicationContext.getBeansOfType(ConsentableAttributeBuilder.class).values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());
        AnnotationAwareOrderComparator.sortIfNecessary(builders);

        val consentableAttributes = new ArrayList<CasConsentableAttribute>();
        LOGGER.debug("Preparing consentable attributes [{}]", attributes);
        attributes.forEach((key, value) -> {
            var attr = CasConsentableAttribute.builder()
                .name(key)
                .values(value)
                .build();

            for (val builder : builders) {
                LOGGER.trace("Preparing to build consentable attribute [{}] via [{}]", attr, builder.getName());
                attr = builder.build(attr);
                LOGGER.trace("Finalized consentable attribute [{}]", attr);
            }
            consentableAttributes.add(attr);
        });
        context.getFlowScope().put("attributes", consentableAttributes);
    }
}
