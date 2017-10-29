package org.apereo.cas.discovery;

import org.apereo.cas.services.RegisteredService;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CasServerProfileRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasServerProfileRegistrar {

    private final CasServerProfile profile;

    public CasServerProfileRegistrar() {
        this.profile = new CasServerProfile();
        profile.setRegisteredServiceTypes(locateRegisteredServiceTypes());
        return;
    }

    private Map<String, Class> locateRegisteredServiceTypes() {
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("org.apereo.cas"))
                        .setScanners(new SubTypesScanner(false)));
        final Set<Class<?>> subTypes = (Set) reflections.getSubTypesOf(RegisteredService.class);
        return subTypes
                .stream()
                .filter(c -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()))
                .map(c -> {
                    try {
                        final RegisteredService svc = (RegisteredService) c.newInstance();
                        return svc;
                    } catch (final Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RegisteredService::getFriendlyName, RegisteredService::getClass));
    }

    public CasServerProfile getProfile() {
        return this.profile;
    }
}
