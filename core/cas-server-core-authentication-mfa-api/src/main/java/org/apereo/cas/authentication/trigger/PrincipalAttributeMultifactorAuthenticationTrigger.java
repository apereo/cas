package org.apereo.cas.authentication.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest, final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val principal = getPrincipalForMultifactorAuthentication(authentication);
        val result = resolveMultifactorAuthenticationProvider(Optional.empty(), registeredService, principal);
        if (result != null && !result.isEmpty()) {
            val id = CollectionUtils.firstElement(result);
            if (id.isEmpty()) {
                return Optional.empty();
            }
            return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(id.get().toString(), applicationContext);
        }

        return Optional.empty();
    }

    /**
     * Gets principal attributes for multifactor authentication.
     *
     * @param authentication the authentication
     * @return the principal attributes for multifactor authentication
     */
    protected Principal getPrincipalForMultifactorAuthentication(final Authentication authentication) {
        return authentication.getPrincipal();
    }

    /**
     * Resolve multifactor authentication provider set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @return the set
     */
    protected Set<Event> resolveMultifactorAuthenticationProvider(final Optional<RequestContext> context,
                                                                  final RegisteredService service,
                                                                  final Principal principal) {
        val globalPrincipalAttributeValueRegex = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex();
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val providers = providerMap.values();
        if (providers.size() == 1 && StringUtils.isNotBlank(globalPrincipalAttributeValueRegex)) {
            return resolveSingleMultifactorProvider(context, service, principal, providers);
        }

        return resolveMultifactorProviderViaPredicate(context, service, principal, providers);
    }

    /**
     * Resolve multifactor provider by regex predicate set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @param providers the providers
     * @return the set
     */
    protected Set<Event> resolveMultifactorProviderViaPredicate(final Optional<RequestContext> context,
                                                                final RegisteredService service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {
        val attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers());
        return multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
            input -> providers.stream().anyMatch(provider -> input != null && provider.matches(input)));
    }

    /**
     * Resolve single multifactor provider set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @param providers the providers
     * @return the set
     */
    protected Set<Event> resolveSingleMultifactorProvider(final Optional<RequestContext> context, final RegisteredService service,
                                                          final Principal principal,
                                                          final Collection<MultifactorAuthenticationProvider> providers) {
        val globalPrincipalAttributeValueRegex = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex();
        val provider = providers.iterator().next();
        LOGGER.trace("Found a single multifactor provider [{}] in the application context", provider);
        val attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers());
        return multifactorAuthenticationProviderResolver.resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
            input -> input != null && input.matches(globalPrincipalAttributeValueRegex));
    }
}
