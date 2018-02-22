package org.apereo.cas.authentication;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default MFA Trigger selection strategy. This strategy looks for valid triggers in the following order: request
 * parameter, RegisteredService policy, principal attribute.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultMultifactorTriggerSelectionStrategy implements MultifactorTriggerSelectionStrategy {
    private final String globalPrincipalAttributeNameTriggers;
    private final String requestParameter;

    @Override
    public Optional<String> resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                    final HttpServletRequest request, final RegisteredService service,
                                    final Principal principal) {
        Optional<String> provider = Optional.empty();

        // short-circuit if we don't have any available MFA providers
        if (providers == null || providers.isEmpty()) {
            return provider;
        }
        final Set<String> validProviderIds = providers.stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.toSet());

        // check for an opt-in provider id parameter trigger, we only care about the first value
        provider = resolveRequestParameterTrigger(request, validProviderIds);

        // check for a RegisteredService configured trigger
        if (!provider.isPresent() && service != null) {
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (shouldApplyRegisteredServiceMultifactorPolicy(policy, principal)) {
                provider = policy.getMultifactorAuthenticationProviders().stream()
                        .filter(validProviderIds::contains)
                        .findFirst();
            }
        }

        // check for Global principal attribute trigger
        if (!provider.isPresent()) {
            provider = resolvePrincipalAttributeTrigger(principal, validProviderIds);
        }

        // return the resolved trigger
        return provider;
    }

    private static boolean shouldApplyRegisteredServiceMultifactorPolicy(final RegisteredServiceMultifactorPolicy policy, final Principal principal) {
        final String attrName = policy.getPrincipalAttributeNameTrigger();
        final String attrValue = policy.getPrincipalAttributeValueToMatch();

        // Principal attribute name and/or value is not defined
        if (!StringUtils.hasText(attrName) || !StringUtils.hasText(attrValue)) {
            return true;
        }

        // no Principal, we should enforce policy
        if (principal == null) {
            return true;
        }

        // check to see if any of the specified attributes match the attrValue pattern
        final Predicate<String> attrValuePredicate = Pattern.compile(attrValue).asPredicate();
        return commaDelimitedListToSet(attrName).stream()
                .map(principal.getAttributes()::get)
                .filter(Objects::nonNull)
                .map(CollectionUtils::toCollection)
                .flatMap(Set::stream)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(attrValuePredicate);
    }

    /**
     * Checks for an opt-in provider id parameter trigger, we only care about the first value.
     */
    private Optional<String> resolveRequestParameterTrigger(final HttpServletRequest request,
                                                            final Set<String> providerIds) {
        return Optional.ofNullable(request)
                .map(r -> r.getParameter(requestParameter))
                .filter(providerIds::contains);
    }

    private Optional<String> resolvePrincipalAttributeTrigger(final Principal principal,
                                                              final Set<String> providerIds) {
        if (principal != null && StringUtils.hasText(globalPrincipalAttributeNameTriggers)) {
            return resolveAttributeTrigger(principal.getAttributes(), globalPrincipalAttributeNameTriggers,
                    providerIds);
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> resolveAttributeTrigger(final Map<String, Object> attributes, final String names,
                                                     final Set<String> providerIds) {
        return commaDelimitedListToSet(names).stream()
                // principal.getAttribute(name).values
                .map(attributes::get).filter(Objects::nonNull)
                .map(CollectionUtils::toCollection).flatMap(Set::stream)
                // validProviderIds.contains((String) value)
                .filter(String.class::isInstance).map(String.class::cast).filter(providerIds::contains)
                .findFirst();
    }
}
