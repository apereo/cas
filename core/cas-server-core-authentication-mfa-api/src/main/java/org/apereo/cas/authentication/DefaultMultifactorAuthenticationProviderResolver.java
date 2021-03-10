package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationProviderResolver implements MultifactorAuthenticationProviderResolver {
    private final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver;

    public DefaultMultifactorAuthenticationProviderResolver(final MultifactorAuthenticationPrincipalResolver resolver) {
        this.multifactorAuthenticationPrincipalResolver = List.of(resolver);
    }

    @Override
    public Set<Event> resolveEventViaAttribute(final Principal principal,
                                               final Map<String, List<Object>> attributesToExamine,
                                               final Collection<String> attributeNames,
                                               final RegisteredService service,
                                               final Optional<RequestContext> context,
                                               final Collection<MultifactorAuthenticationProvider> providers,
                                               final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {

        LOGGER.debug("Attributes to examine are [{}]", attributesToExamine);
        if (providers == null || providers.isEmpty()) {
            LOGGER.debug("No authentication provider is associated with this service");
            return null;
        }

        LOGGER.debug("Locating attribute value for attribute(s): [{}].", attributeNames);
        for (val attributeName : attributeNames) {
            val attributeValue = attributesToExamine.get(attributeName);
            if (attributeValue == null) {
                LOGGER.debug("Attribute value for [{}] to determine event is not configured for [{}]", attributeName, principal.getId());
                continue;
            }
            LOGGER.debug("Located attribute value [{}] for [{}]", attributeValue, attributeNames);

            for (val provider : providers) {
                var results = MultifactorAuthenticationUtils.resolveEventViaSingleAttribute(principal, attributeValue,
                    service, context, provider, predicate);
                if (results == null || results.isEmpty()) {
                    results = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(principal, attributeValue,
                        service, context, provider, predicate);
                }
                if (results != null && !results.isEmpty()) {
                    LOGGER.debug("Resolved set of events based on the attribute [{}] are [{}]", attributeName, results);
                    return results;
                }
            }
        }
        LOGGER.debug("No set of events based on the attribute(s) [{}] could be matched", attributeNames);
        return null;
    }

    @Override
    public Principal resolvePrincipal(final Principal principal) {
        return multifactorAuthenticationPrincipalResolver
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }
}
