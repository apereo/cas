package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RankedAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RankedAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {


    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    private final AuthenticationContextValidator authenticationContextValidator;

    public RankedAuthenticationProviderWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                            final CentralAuthenticationService centralAuthenticationService,
                                                            final ServicesManager servicesManager,
                                                            final TicketRegistrySupport ticketRegistrySupport,
                                                            final CookieGenerator warnCookieGenerator,
                                                            final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                            final MultifactorAuthenticationProviderSelector selector,
                                                            final AuthenticationContextValidator authenticationContextValidator,
                                                            final CasDelegatingWebflowEventResolver casDelegatingWebflowEventResolver) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
        this.authenticationContextValidator = authenticationContextValidator;
        this.initialAuthenticationAttemptWebflowEventResolver = casDelegatingWebflowEventResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val tgt = WebUtils.getTicketGrantingTicketId(context);
        val service = WebUtils.getRegisteredService(context);

        if (service == null) {
            LOGGER.debug("No service is available to determine event for principal");
            return resumeFlow();
        }

        if (StringUtils.isBlank(tgt)) {
            LOGGER.trace("TGT is blank; proceed with flow normally.");
            return resumeFlow();
        }
        val authentication = this.ticketRegistrySupport.getAuthenticationFrom(tgt);
        if (authentication == null) {
            LOGGER.trace("TGT has no authentication and is blank; proceed with flow normally.");
            return resumeFlow();
        }

        val credential = WebUtils.getCredential(context);
        val builder = this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);

        LOGGER.debug("Recording and tracking initial authentication results in the request context");
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        val event = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            LOGGER.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow();
        }

        val id = event.getId();
        LOGGER.debug("Resolved event from the initial authentication leg is [{}]", id);

        if (id.equals(CasWebflowConstants.TRANSITION_ID_ERROR)
            || id.equals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
            || id.equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)
            || id.equals(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS)) {
            LOGGER.debug("Returning webflow event as [{}]", id);
            return CollectionUtils.wrapSet(event);
        }

        LOGGER.debug("Validating authentication context for event [{}] and service [{}]", id, service);
        val result = this.authenticationContextValidator.validate(authentication, id, service);

        if (result.getKey()) {
            LOGGER.debug("Authentication context is successfully validated by [{}] for service [{}]", id, service);
            return resumeFlow();
        }

        val value = result.getValue();
        if (value.isPresent()) {
            val attributeMap = buildEventAttributeMap(authentication.getPrincipal(), service, value.get());
            return CollectionUtils.wrapSet(validateEventIdForMatchingTransitionInContext(id, context, attributeMap));
        }
        LOGGER.warn("The authentication context cannot be satisfied and the requested event [{}] is unrecognized", id);
        return CollectionUtils.wrapSet(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    private Set<Event> resumeFlow() {
        return CollectionUtils.wrapSet(new EventFactorySupport().success(this));
    }
}
