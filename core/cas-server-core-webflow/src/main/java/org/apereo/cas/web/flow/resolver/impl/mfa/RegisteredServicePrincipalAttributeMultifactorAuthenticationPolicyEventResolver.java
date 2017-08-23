package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is {@link RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver}
 * that attempts to locate the given principal attribute in the service authentication policy
 * and match it against the pattern provided in the same policy.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver.class);

    public RegisteredServicePrincipalAttributeMultifactorAuthenticationPolicyEventResolver(
            final AuthenticationSystemSupport authenticationSystemSupport,
            final CentralAuthenticationService centralAuthenticationService,
            final ServicesManager servicesManager,
            final TicketRegistrySupport ticketRegistrySupport,
            final CookieGenerator warnCookieGenerator,
            final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
            final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (authentication == null || service == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return null;
        }

        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy == null || service.getMultifactorPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.debug("Authentication policy is absent or does not contain any multifactor authentication providers");
            return null;
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())
                || StringUtils.isBlank(policy.getPrincipalAttributeValueToMatch())) {
            LOGGER.debug("Authentication policy does not define a principal attribute and/or value to trigger multifactor authentication");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        final Collection<MultifactorAuthenticationProvider> providers = flattenProviders(getAuthenticationProviderForService(service));
        return resolveEventViaPrincipalAttribute(principal,
                org.springframework.util.StringUtils.commaDelimitedListToSet(policy.getPrincipalAttributeNameTrigger()),
                service, context, providers, Pattern.compile(policy.getPrincipalAttributeValueToMatch()).asPredicate());
    }


    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
