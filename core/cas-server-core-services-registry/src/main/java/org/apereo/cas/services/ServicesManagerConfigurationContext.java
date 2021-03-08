package org.apereo.cas.services;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
    private final ServiceRegistry serviceRegistry;

    private final ConfigurableApplicationContext applicationContext;

    @Builder.Default
    private final Set<String> environments = new HashSet<>();

    private final Cache<Long, RegisteredService> servicesCache;

    @Builder.Default
    private final List<ServicesManagerRegisteredServiceLocator> registeredServiceLocators = new ArrayList<>();
}
