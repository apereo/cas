package org.apereo.cas.discovery;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
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
@Setter
@RequiredArgsConstructor
public class CasServerProfileRegistrar implements ApplicationContextAware {
    private final Clients clients;
    private final Set<String> availableAttributes;
    private ApplicationContext applicationContext;

    private Map<String, String> locateMultifactorAuthenticationProviderTypesSupported() {
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        return providers
            .values()
            .stream()
            .collect(Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName));
    }

    private static Map<String, Class> locateRegisteredServiceTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                return (RegisteredService) c.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                return null;
            }
        };
        val collector = Collectors.toMap(RegisteredService::getFriendlyName, RegisteredService::getClass);
        return (Map) locateSubtypesByReflection(mapper, collector,
            AbstractRegisteredService.class, o -> true, CentralAuthenticationService.NAMESPACE);
    }

    private static Object locateSubtypesByReflection(final Function<Class, Object> mapper, final Collector collector,
                                                     final Class parentType, final Predicate filter, final String packageNamespace) {
        val reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageNamespace))
            .setScanners(new SubTypesScanner(false)));
        val subTypes = (Set<Class<?>>) reflections.getSubTypesOf(parentType);
        return subTypes.stream()
            .filter(c -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && filter.test(c))
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(collector);
    }

    private Set<String> locateDelegatedClientTypesSupported() {
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
        val profile = new CasServerProfile();
        profile.setRegisteredServiceTypesSupported(locateRegisteredServiceTypesSupported());
        profile.setMultifactorAuthenticationProviderTypesSupported(locateMultifactorAuthenticationProviderTypesSupported());
        profile.setDelegatedClientTypesSupported(locateDelegatedClientTypesSupported());
        profile.setAvailableAttributes(this.availableAttributes);
        return profile;
    }
}
