package org.apereo.cas.discovery;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ReflectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Modifier;
import java.util.Collection;
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
    private final CasConfigurationProperties casProperties;

    private final Clients clients;

    private final Set<String> availableAttributes;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private ApplicationContext applicationContext;

    private static Map<String, Class<?>> locateRegisteredServiceTypesSupported() {
        final Function<Class<?>, RegisteredService> mapper = c -> {
            try {
                return (RegisteredService) c.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                return null;
            }
        };
        val collector = Collectors.toMap(RegisteredService::getFriendlyName, RegisteredService::getClass);
        return (Map) locateSubtypesByReflection(mapper, collector,
            BaseRegisteredService.class, o -> true, CentralAuthenticationService.NAMESPACE);
    }

    private static <T, R> R locateSubtypesByReflection(final Function<Class<?>, T> mapper,
                                                       final Collector<T, ?, R> collector,
                                                       final Class<?> parentType, final Predicate<Class<?>> filter,
                                                       final String packageNamespace) {

        Collection<? extends Class<?>> subTypes = ReflectionUtils.findSubclassesInPackage(parentType, packageNamespace);

        return subTypes.stream()
                .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
                .filter(filter)
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
        val profile = new CasServerProfile();
        profile.setRegisteredServiceTypesSupported(locateRegisteredServiceTypesSupported());
        profile.setMultifactorAuthenticationProviderTypesSupported(locateMultifactorAuthenticationProviderTypesSupported());
        profile.setDelegatedClientTypesSupported(locateDelegatedClientTypesSupported());
        profile.setAvailableAttributes(this.availableAttributes);
        profile.setUserDefinedScopes(casProperties.getAuthn().getOidc().getCore().getUserDefinedScopes().keySet());
        profile.setAvailableAuthenticationHandlers(locateAvailableAuthenticationHandlers());
        profile.setTicketTypesSupported(locateTicketTypesSupported());
        return profile;
    }

    private Map<String, Map<String, Object>> locateTicketTypesSupported() {
        val catalog = applicationContext.getBean(TicketCatalog.BEAN_NAME, TicketCatalog.class);
        return catalog
            .findAll()
            .stream()
            .collect(Collectors.toMap(TicketDefinition::getPrefix,
                value -> CollectionUtils.wrap("storageName", value.getProperties().getStorageName(),
                "storageTimeout", value.getProperties().getStorageTimeout())));
    }

    private Map<String, String> locateMultifactorAuthenticationProviderTypesSupported() {
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        return providers
            .values()
            .stream()
            .collect(Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName));
    }

    private Set<String> locateDelegatedClientTypesSupported() {
        if (clients == null) {
            return new LinkedHashSet<>(0);
        }
        return clients.findAllClients().stream().map(Client::getName).collect(Collectors.toSet());
    }

    private Set<String> locateAvailableAuthenticationHandlers() {
        return this.authenticationEventExecutionPlan.getAuthenticationHandlers()
            .stream()
            .map(AuthenticationHandler::getName)
            .collect(Collectors.toSet());
    }
}
