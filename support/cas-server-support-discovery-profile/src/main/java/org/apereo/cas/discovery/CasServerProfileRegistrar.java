package org.apereo.cas.discovery;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This is {@link CasServerProfileRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class CasServerProfileRegistrar implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;
    private final Clients clients;

    private Map<String, String> locateMultifactorAuthenticationProviderTypesActive() {
        return MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext)
            .values().stream().collect(Collectors.toMap(MultifactorAuthenticationProvider::getId,
                MultifactorAuthenticationProvider::getFriendlyName));
    }

    private Map<String, String> locateMultifactorAuthenticationProviderTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                final MultifactorAuthenticationProvider p = MultifactorAuthenticationProvider.class.cast(c.getDeclaredConstructor().newInstance());
                LOGGER.debug("Located supported multifactor authentication provider [{}]", p.getId());
                return p;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }
        };
        final Predicate filter = o -> !VariegatedMultifactorAuthenticationProvider.class.isAssignableFrom(Class.class.cast(o));
        final Collector collector = Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName);
        return (Map) locateSubtypesByReflection(mapper, collector,
            AbstractMultifactorAuthenticationProvider.class, filter, CentralAuthenticationService.NAMESPACE);
    }

    private Map<String, Class> locateRegisteredServiceTypesActive() {
        return this.servicesManager.getAllServices().stream().map(svc -> Pair.of(svc.getFriendlyName(), svc.getClass())).distinct().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Map<String, Class> locateRegisteredServiceTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                return (RegisteredService) c.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                return null;
            }
        };
        final Collector collector = Collectors.toMap(RegisteredService::getFriendlyName, RegisteredService::getClass);
        return (Map) locateSubtypesByReflection(mapper, collector,
            AbstractRegisteredService.class, Predicates.alwaysTrue(), CentralAuthenticationService.NAMESPACE);
    }

    private Object locateSubtypesByReflection(final Function<Class, Object> mapper, final Collector collector,
                                              final Class parentType, final Predicate filter, final String packageNamespace) {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageNamespace))
            .setScanners(new SubTypesScanner(false)));
        final Set<Class<?>> subTypes = (Set) reflections.getSubTypesOf(parentType);
        return subTypes.stream()
            .filter(c -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && filter.test(c))
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(collector);
    }

    private Set<String> locateDelegatedClientTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                return IndirectClient.class.cast(c.getDeclaredConstructor().newInstance()).getName();
            } catch (final Exception e) {
                return null;
            }
        };
        return (Set) locateSubtypesByReflection(mapper, Collectors.toSet(), IndirectClient.class,
            Predicates.alwaysTrue(), "org.pac4j");
    }

    private Set<String> locateDelegatedClientTypes() {
        if (clients == null) {
            return new LinkedHashSet<>(0);
        }
        return clients.findAllClients().stream().map(Client::getName).collect(Collectors.toSet());
    }

    /**
     * Gets profile.
     *
     * @return the profile
     */
    public CasServerProfile getProfile() {
        final CasServerProfile profile = new CasServerProfile();
        profile.setRegisteredServiceTypesSupported(locateRegisteredServiceTypesSupported());
        profile.setRegisteredServiceTypes(locateRegisteredServiceTypesActive());
        profile.setMultifactorAuthenticationProviderTypesSupported(locateMultifactorAuthenticationProviderTypesSupported());
        profile.setMultifactorAuthenticationProviderTypes(locateMultifactorAuthenticationProviderTypesActive());
        profile.setDelegatedClientTypesSupported(locateDelegatedClientTypesSupported());
        profile.setDelegatedClientTypes(locateDelegatedClientTypes());
        return profile;
    }
}
