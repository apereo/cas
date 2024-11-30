package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class PredicatedPrincipalAttributeMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private static final Class[] PREDICATE_CTOR_PARAMETERS = {Object.class, Object.class, Object.class, Object.class};

    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) {
        val properties = casProperties.getAuthn().getMfa().getTriggers().getPrincipal();
        val predicateResource = Objects.requireNonNull(properties.getGlobalPrincipalAttributePredicate().getLocation());
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        val providers = providerMap.values();

        if (providers.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val args = new Object[]{service, principal, providers, LOGGER};
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val predicate = scriptFactory.newObjectInstance(predicateResource, PREDICATE_CTOR_PARAMETERS, args, Predicate.class);
        if (predicate == null) {
            LOGGER.debug("No multifactor authentication provider is determined by the predicate");
            return Optional.empty();
        }

        LOGGER.debug("Created predicate instance [{}] from [{}] to filter multifactor authentication providers [{}]",
            predicate.getClass().getSimpleName(), predicateResource, providers);

        return providers
            .stream()
            .filter(predicate)
            .min(Comparator.comparingInt(MultifactorAuthenticationProvider::getOrder));
    }
}
