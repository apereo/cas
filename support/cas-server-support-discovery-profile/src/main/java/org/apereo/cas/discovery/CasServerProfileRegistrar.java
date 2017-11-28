package org.apereo.cas.discovery;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Modifier;
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
public class CasServerProfileRegistrar implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasServerProfileRegistrar.class);

    private ApplicationContext applicationContext;

    private final ServicesManager servicesManager;

    public CasServerProfileRegistrar(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    private Map<String, String> locateMultifactorAuthenticationProviderTypesActive() {
        return MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext)
                .values()
                .stream()
                .collect(Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName));
    }

    private Map<String, String> locateMultifactorAuthenticationProviderTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                final MultifactorAuthenticationProvider p = MultifactorAuthenticationProvider.class.cast(c.newInstance());
                LOGGER.debug("Located supported multifactor authentication provider [{}]", p.getId());
                return p;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }
        };

        final Predicate filter = o -> !VariegatedMultifactorAuthenticationProvider.class.isAssignableFrom(Class.class.cast(o));
        final Collector collector = Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName);
        return (Map) locateSubtypesByReflection(mapper, collector, AbstractMultifactorAuthenticationProvider.class, filter);
    }

    private Map<String, Class> locatedRegisteredServiceTypesActive() {
        return this.servicesManager.getAllServices()
                .stream()
                .map(svc -> Pair.of(svc.getFriendlyName(), svc.getClass()))
                .distinct()
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Map<String, Class> locatedRegisteredServiceTypesSupported() {
        final Function<Class, Object> mapper = c -> {
            try {
                final RegisteredService svc = (RegisteredService) c.newInstance();
                return svc;
            } catch (final Exception e) {
                return null;
            }
        };

        final Collector collector = Collectors.toMap(RegisteredService::getFriendlyName, RegisteredService::getClass);
        return (Map) locateSubtypesByReflection(mapper, collector, AbstractRegisteredService.class, Predicates.alwaysTrue());
    }

    private Object locateSubtypesByReflection(final Function<Class, Object> mapper,
                                              final Collector collector,
                                              final Class parentType,
                                              final Predicate filter) {
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(CentralAuthenticationService.NAMESPACE))
                        .setScanners(new SubTypesScanner(false)));
        final Set<Class<?>> subTypes = (Set) reflections.getSubTypesOf(parentType);
        return subTypes
                .stream()
                .filter(c -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && filter.test(c))
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(collector);
    }

    /**
     * Gets profile.
     *
     * @return the profile
     */
    public CasServerProfile getProfile() {
        final CasServerProfile profile = new CasServerProfile();

        profile.setRegisteredServiceTypesSupported(locatedRegisteredServiceTypesSupported());
        profile.setRegisteredServiceTypes(locatedRegisteredServiceTypesActive());

        profile.setMultifactorAuthenticationProviderTypesSupported(locateMultifactorAuthenticationProviderTypesSupported());
        profile.setMultifactorAuthenticationProviderTypes(locateMultifactorAuthenticationProviderTypesActive());

        return profile;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
