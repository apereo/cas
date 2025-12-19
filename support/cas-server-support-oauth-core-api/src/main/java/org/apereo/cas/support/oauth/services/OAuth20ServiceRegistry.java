package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OAuth20ServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20ServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public OAuth20ServiceRegistry(final List<RegisteredService> services,
                                  final ConfigurableApplicationContext applicationContext) {
        super(services, applicationContext, new ArrayList<>());
    }

    public OAuth20ServiceRegistry(final ConfigurableApplicationContext applicationContext, final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()), applicationContext);
    }
}
