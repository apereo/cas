package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ServicesManagerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuperBuilder
@Getter
public class ServicesManagerConfigurationContext {

    /**
     * Implementation bean name.
     */
    public static final String BEAN_NAME = "servicesManagerConfigurationContext";

    @NonNull
    private final ServiceRegistry serviceRegistry;

    @NonNull
    private final ConfigurableApplicationContext applicationContext;

    @Builder.Default
    private final Set<String> environments = new HashSet<>();

    @NonNull
    private final Cache<@NonNull Long, RegisteredService> servicesCache;

    @Builder.Default
    private final List<ServicesManagerRegisteredServiceLocator> registeredServiceLocators = new ArrayList<>();

    @NonNull
    private final RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @NonNull
    private final CasConfigurationProperties casProperties;

    @NonNull
    private final TenantExtractor tenantExtractor;

    @NonNull
    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final RegisteredServiceIndexService registeredServiceIndexService;
}
