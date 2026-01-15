package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class PrincipalAttributeMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private final ApplicationContext applicationContext;

    private final TenantExtractor tenantExtractor;
    
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   @Nullable final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   @Nullable final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val principal = getPrincipalForMultifactorAuthentication(authentication);
        val result = resolveMultifactorAuthenticationProvider(Optional.empty(), registeredService, service, principal);
        if (result != null && !result.isEmpty()) {
            val id = CollectionUtils.firstElement(result);
            return id.flatMap(o -> MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(o.toString(), applicationContext));
        }

        return Optional.empty();
    }

    protected Principal getPrincipalForMultifactorAuthentication(final Authentication authentication) {
        return authentication.getPrincipal();
    }

    protected Set<Event> resolveMultifactorAuthenticationProvider(final Optional<RequestContext> context,
                                                                  @Nullable final RegisteredService registeredService,
                                                                  @Nullable final Service service,
                                                                  final Principal principal) {
        val events = determineMultifactorAuthenticationEvent(context, registeredService, service, principal);
        val deny = casProperties.getAuthn().getMfa().getTriggers().getPrincipal().isDenyIfUnmatched();
        if (deny && (events == null || events.isEmpty())) {
            throw new AuthenticationException(new MultifactorAuthenticationRequiredException(registeredService, principal));
        }
        return events;
    }

    protected Set<Event> determineMultifactorAuthenticationEvent(final Optional<RequestContext> context,
                                                                 @Nullable final RegisteredService registeredService,
                                                                 @Nullable final Service service,
                                                                 final Principal principal) {
        val globalPrincipalAttributeValueRegex = casProperties.getAuthn().getMfa()
            .getTriggers().getPrincipal().getGlobalPrincipalAttributeValueRegex();
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val providers = providerMap.values();
        if (providers.size() == 1 && StringUtils.isNotBlank(globalPrincipalAttributeValueRegex)) {
            return resolveSingleMultifactorProvider(context, registeredService, service, principal, providers);
        }

        return resolveMultifactorProviderViaPredicate(context, registeredService, service, principal, providers);
    }

    protected Set<Event> resolveMultifactorProviderViaPredicate(final Optional<RequestContext> context,
                                                                final RegisteredService registeredService,
                                                                final Service service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {
        val attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa()
            .getTriggers().getPrincipal().getGlobalPrincipalAttributeNameTriggers());
        return multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(
            principal, attributeNames, registeredService, service, context, providers,
            (attributeValue, provider) -> attributeValue != null && provider.matches(attributeValue));
    }

    protected Set<Event> resolveSingleMultifactorProvider(final Optional<RequestContext> context,
                                                          @Nullable final RegisteredService registeredService,
                                                          @Nullable final Service service,
                                                          final Principal principal,
                                                          final Collection<MultifactorAuthenticationProvider> providers) {
        val properties = casProperties.getAuthn().getMfa().getTriggers().getPrincipal();
        val globalPrincipalAttributeValueRegex = properties.getGlobalPrincipalAttributeValueRegex();

        val provider = providers.iterator().next();
        LOGGER.trace("Found a single multifactor provider [{}] in the application context", provider);
        val attributeNames = commaDelimitedListToSet(properties.getGlobalPrincipalAttributeNameTriggers());
        return multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(
            principal, attributeNames, registeredService, service, context, providers,
            new PatternedAttributeValuePredicate(globalPrincipalAttributeValueRegex, properties.isReverseMatch()));
    }

    @RequiredArgsConstructor
    private static final class PatternedAttributeValuePredicate implements BiPredicate<String, MultifactorAuthenticationProvider> {
        private final String pattern;
        private final boolean reverseMatch;

        @Override
        public boolean test(final String attributeValue, final MultifactorAuthenticationProvider multifactorAuthenticationProvider) {
            return attributeValue != null && reverseMatch != RegexUtils.find(pattern, attributeValue);
        }
    }
}
