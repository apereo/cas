package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Nonnull
    private final ServiceRegistry serviceRegistry;

    @Nonnull
    private final ConfigurableApplicationContext applicationContext;

    @Builder.Default
    private final Set<String> environments = new HashSet<>();

    @Nonnull
    private final Cache<Long, RegisteredService> servicesCache;

    @Builder.Default
    private final List<ServicesManagerRegisteredServiceLocator> registeredServiceLocators = new ArrayList<>();

    @Nonnull
    private final RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @Nonnull
    private final CasConfigurationProperties casProperties;

    @Nonnull
    private final TenantExtractor tenantExtractor;

    @Nonnull
    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final RegisteredServiceIndexService registeredServiceIndexService;
}
