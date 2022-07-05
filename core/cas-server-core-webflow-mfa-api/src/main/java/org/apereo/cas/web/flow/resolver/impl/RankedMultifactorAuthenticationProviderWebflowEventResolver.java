package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RankedMultifactorAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RankedMultifactorAuthenticationProviderWebflowEventResolver extends AbstractCasMultifactorAuthenticationWebflowEventResolver
    implements CasDelegatingWebflowEventResolver {

    private final CasDelegatingWebflowEventResolver casDelegatingWebflowEventResolver;

    private final MultifactorAuthenticationContextValidator authenticationContextValidator;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    public RankedMultifactorAuthenticationProviderWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext,
        final CasDelegatingWebflowEventResolver casDelegatingWebflowEventResolver,
        final MultifactorAuthenticationContextValidator authenticationContextValidator,
        final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy) {

        super(configurationContext);
        this.casDelegatingWebflowEventResolver = casDelegatingWebflowEventResolver;
        this.authenticationContextValidator = authenticationContextValidator;
        this.singleSignOnParticipationStrategy = singleSignOnParticipationStrategy;
    }

    private static Set<Event> buildEventForMultifactorProvider(final RequestContext context,
                                                               final RegisteredService service,
                                                               final Authentication authentication,
                                                               final String id,
                                                               final MultifactorAuthenticationProvider provider) {
        val attributeMap = MultifactorAuthenticationUtils.buildEventAttributeMap(authentication.getPrincipal(), Optional.of(service), provider);
        LOGGER.trace("Event attribute map for [{}] is [{}]", id, attributeMap);
        val resultEvent = MultifactorAuthenticationUtils.validateEventIdForMatchingTransitionInContext(id, Optional.of(context), attributeMap);
        LOGGER.trace("Finalized event for multifactor provider  [{}] is [{}]", id, resultEvent);
        return CollectionUtils.wrapSet(resultEvent);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val tgt = WebUtils.getTicketGrantingTicketId(context);
        val service = WebUtils.getRegisteredService(context);

        if (service == null) {
            LOGGER.debug("No service is available to determine event for principal");
            return resumeFlow(context);
        }

        if (StringUtils.isBlank(tgt)) {
            LOGGER.trace("Ticket-granting ticket is blank; proceed with flow normally.");
            return resumeFlow(context);
        }
        val authentication = getConfigurationContext().getTicketRegistrySupport().getAuthenticationFrom(tgt);
        if (authentication == null) {
            LOGGER.trace("Ticket-granting ticket has no authentication and is blank; proceed with flow normally.");
            return resumeFlow(context);
        }

        val credential = WebUtils.getCredential(context);
        val builder = getConfigurationContext().getAuthenticationSystemSupport()
            .establishAuthenticationContextFromInitial(authentication, credential);

        LOGGER.trace("Recording and tracking initial authentication results in the request context");
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .requestContext(context)
            .build();
        if (singleSignOnParticipationStrategy.supports(ssoRequest) && !singleSignOnParticipationStrategy.isParticipating(ssoRequest)) {
            LOGGER.debug("Cannot proceed with existing authenticated session for [{}] since the single sign-on participation "
                         + "strategy for this request could now allow participation in the current session.", authentication);
            return resumeFlow(context);
        }

        val event = casDelegatingWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            LOGGER.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow(context);
        }

        val id = event.getId();
        LOGGER.trace("Resolved event from the initial authentication leg is [{}]", id);

        if (getOperableTransitions().contains(id)) {
            LOGGER.trace("Returning webflow event as [{}]", id);
            return CollectionUtils.wrapSet(event);
        }

        LOGGER.trace("Validating authentication context for event [{}] and service [{}]", id, service);
        val result = authenticationContextValidator.validate(authentication, id, Optional.of(service));
        val validatedProvider = result.getProvider();

        if (result.isSuccess()) {
            if (service.getMultifactorAuthenticationPolicy().isForceExecution() && validatedProvider.isPresent()) {
                val provider = validatedProvider.get();
                LOGGER.trace("Multifactor authentication policy for [{}] is set to force execution for [{}]", service, provider);
                return buildEventForMultifactorProvider(context, service, authentication, id, provider);
            }
            LOGGER.debug("Authentication context is successfully validated by [{}] for service [{}]", id, service);
            return resumeFlow(context);
        }

        if (validatedProvider.isPresent()) {
            val provider = validatedProvider.get();
            return buildEventForMultifactorProvider(context, service, authentication, id, provider);
        }

        LOGGER.warn("The authentication context cannot be satisfied and the requested event [{}] is unrecognized", id);
        return CollectionUtils.wrapSet(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));
    }

    @Audit(action = AuditableActions.AUTHENTICATION_EVENT,
        actionResolverName = AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER)
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver resolver) {
        casDelegatingWebflowEventResolver.addDelegate(resolver);
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver resolver, final int index) {
        casDelegatingWebflowEventResolver.addDelegate(resolver, index);
    }

    protected Set<Event> resumeFlow(final RequestContext context) {
        val success = new EventFactorySupport().event(this,
            CasWebflowConstants.TRANSITION_ID_SUCCESS, new LocalAttributeMap<>());
        return CollectionUtils.wrapSet(success);
    }

    private static List<String> getOperableTransitions() {
        val events = new ArrayList<String>();
        events.add(CasWebflowConstants.TRANSITION_ID_ERROR);
        events.add(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
        events.add(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        events.add(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS);
        events.add(CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE);
        return events;
    }
}
