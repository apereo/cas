package org.apereo.cas.discovery;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasServerProfileRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class DefaultCasServerProfileRegistrar implements CasServerProfileRegistrar {
    protected final CasConfigurationProperties casProperties;
    protected final Set<String> availableAttributes;
    protected final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;
    protected final ApplicationContext applicationContext;

    private static Set<String> locateRegisteredServiceTypesSupported() {
        val subTypes = ReflectionUtils.findSubclassesInPackage(BaseRegisteredService.class, CentralAuthenticationService.NAMESPACE);
        return subTypes
            .stream()
            .filter(type -> !type.isInterface() && !Modifier.isAbstract(type.getModifiers()))
            .map(type -> FunctionUtils.doAndHandle(() -> {
                val service = (RegisteredService) type.getDeclaredConstructor().newInstance();
                return service.getFriendlyName() + '@' + service.getClass().getName();
            }))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }


    @Override
    public CasServerProfile getProfile(final HttpServletRequest request, final HttpServletResponse response) {
        val profile = new CasServerProfile();
        profile.setRegisteredServiceTypesSupported(locateRegisteredServiceTypesSupported());
        profile.setMultifactorAuthenticationProviderTypesSupported(locateMultifactorAuthenticationProviderTypesSupported());
        profile.setAvailableAttributes(this.availableAttributes);
        profile.setAvailableAuthenticationHandlers(locateAvailableAuthenticationHandlers());
        profile.setTicketTypesSupported(locateTicketTypesSupported());
        val customizers = applicationContext.getBeansOfType(CasServerProfileCustomizer.class).values();
        customizers.forEach(customizer -> customizer.customize(profile, request, response));
        return profile;
    }

    private Map<String, Map<String, Object>> locateTicketTypesSupported() {
        val catalog = applicationContext.getBean(TicketCatalog.BEAN_NAME, TicketCatalog.class);
        val ticketFactory = applicationContext.getBean(TicketFactory.BEAN_NAME, TicketFactory.class);

        return catalog
            .findAll()
            .stream()
            .collect(Collectors.toMap(TicketDefinition::getPrefix,
                value -> {
                    val details = new LinkedHashMap<String, Object>();
                    details.put("storageName", value.getProperties().getStorageName());
                    if (value.getProperties().getStorageTimeout() > 0) {
                        details.put("storageTimeout", value.getProperties().getStorageTimeout());
                    }
                    val ticket = ticketFactory.get(value.getApiClass());
                    val expirationPolicy = ticket.getExpirationPolicyBuilder().buildTicketExpirationPolicy();
                    details.put("name", expirationPolicy.getName());
                    details.put("timeToLive", expirationPolicy.getTimeToLive());
                    if (expirationPolicy instanceof final IdleExpirationPolicy iep) {
                        details.put("timeToIdle", iep.getTimeToIdle());
                    }
                    return details;
                }));
    }

    private Map<String, String> locateMultifactorAuthenticationProviderTypesSupported() {
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        return providers
            .values()
            .stream()
            .collect(Collectors.toMap(MultifactorAuthenticationProvider::getId, MultifactorAuthenticationProvider::getFriendlyName));
    }

    private Set<String> locateAvailableAuthenticationHandlers() {
        return authenticationEventExecutionPlan.resolveAuthenticationHandlers()
            .stream()
            .map(AuthenticationHandler::getName)
            .collect(Collectors.toSet());
    }
}
