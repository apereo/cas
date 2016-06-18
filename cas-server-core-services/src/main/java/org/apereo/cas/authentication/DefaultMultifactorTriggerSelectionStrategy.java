package org.apereo.cas.authentication;

import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default MFA Trigger selection strategy. This strategy looks for valid triggers in the following order: request
 * parameter, RegisteredService policy, principal attribute.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
public class DefaultMultifactorTriggerSelectionStrategy implements MultifactorTriggerSelectionStrategy {
    private static final Splitter ATTR_NAMES = Splitter.on(',').trimResults().omitEmptyStrings();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Optional<String> resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                    final HttpServletRequest request, final RegisteredService service, final Principal principal) {
        Optional<String> provider = Optional.empty();

        // short-circuit if we don't have any available MFA providers
        if (providers == null || providers.isEmpty()) {
            return provider;
        }
        final Set<String> validProviderIds = providers.stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.toSet());

        // check for an opt-in provider id parameter trigger, we only care about the first value
        if (!provider.isPresent() && request != null) {
            provider = Optional.ofNullable(request.getParameter(casProperties.getAuthn().getMfa().getRequestParameter()))
                    .filter(validProviderIds::contains);
        }

        // check for a RegisteredService configured trigger
        if (!provider.isPresent() && service != null) {
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (shouldApplyRegisteredServiceMultifactorPolicy(policy, principal)) {
                provider = policy.getMultifactorAuthenticationProviders().stream()
                        .filter(validProviderIds::contains)
                        .findFirst();
            }
        }

        // check for principal attribute trigger
        if (!provider.isPresent() && principal != null && StringUtils.hasText(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers())) {
            provider = StreamSupport.stream(ATTR_NAMES.split(casProperties.getAuthn()
                    .getMfa().getGlobalPrincipalAttributeNameTriggers()).spliterator(), false)
                    // principal.getAttribute(name).values
                    .map(principal.getAttributes()::get).filter(Objects::nonNull)
                    .map(CollectionUtils::convertValueToCollection).flatMap(Set::stream)
                    // validProviderIds.contains((String) value)
                    .filter(String.class::isInstance).map(String.class::cast).filter(validProviderIds::contains)
                    .findFirst();
        }

        // return the resolved trigger
        return provider;
    }

    private static boolean shouldApplyRegisteredServiceMultifactorPolicy(
            final RegisteredServiceMultifactorPolicy policy,
            final Principal principal) {
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
        return StreamSupport.stream(ATTR_NAMES.split(attrName).spliterator(), false)
                // principal.getAttribute(name).values
                .map(principal.getAttributes()::get).filter(Objects::nonNull)
                .map(CollectionUtils::convertValueToCollection).flatMap(Set::stream)
                // value =~ /attrValue/
                .filter(String.class::isInstance).map(String.class::cast)
                .anyMatch(Predicates.containsPattern(attrValue)::apply);
    }

    /**
     * Sets mfa properties.
     *
     * @param multifactorAuthenticationProperties the mfa properties
     */
    public void setMfaProperties(final MultifactorAuthenticationProperties multifactorAuthenticationProperties) {
        this.casProperties.getAuthn().setMfa(multifactorAuthenticationProperties);
    }
}
