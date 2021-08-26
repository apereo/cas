package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class DefaultMultifactorAuthenticationProviderWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private final MultifactorAuthenticationTrigger multifactorAuthenticationTrigger;

    public DefaultMultifactorAuthenticationProviderWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext configurationContext,
                                                                        final MultifactorAuthenticationTrigger multifactorAuthenticationTrigger) {
        super(configurationContext);
        this.multifactorAuthenticationTrigger = multifactorAuthenticationTrigger;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val service = resolveServiceFromAuthenticationRequest(context);
        val authentication = WebUtils.getAuthentication(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val result = determineMultifactorAuthenticationProvider(registeredService, authentication, request, service);
        return result
            .map(provider -> {
                LOGGER.trace("Building event based on the authentication provider [{}] and service [{}]", provider, registeredService);
                val eventMap = MultifactorAuthenticationUtils.buildEventAttributeMap(authentication.getPrincipal(),
                    Optional.ofNullable(registeredService), provider);
                eventMap.put(MultifactorAuthenticationTrigger.class.getSimpleName(), multifactorAuthenticationTrigger.getName());
                val event = MultifactorAuthenticationUtils.validateEventIdForMatchingTransitionInContext(
                    provider.getId(), Optional.of(context), eventMap);
                return CollectionUtils.wrapSet(event);
            })
            .orElse(null);
    }

    @Audit(action = AuditableActions.AUTHENTICATION_EVENT,
        actionResolverName = AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER)
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    private Optional<MultifactorAuthenticationProvider> determineMultifactorAuthenticationProvider(final RegisteredService registeredService,
                                                                                                   final Authentication authentication,
                                                                                                   final HttpServletRequest request,
                                                                                                   final Service service) {
        if (registeredService != null && registeredService.getMultifactorPolicy().isBypassEnabled()) {
            return Optional.empty();
        }
        if (multifactorAuthenticationTrigger.supports(request, registeredService, authentication, service)) {
            return multifactorAuthenticationTrigger.isActivated(authentication, registeredService, request, service);
        }
        return Optional.empty();
    }
}
