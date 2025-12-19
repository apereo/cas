package org.apereo.cas.ws.idp.services;

import module java.base;
import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link WSFederationServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WSFederationServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public WSFederationServiceRegistry(final List<RegisteredService> services,
                                       final ConfigurableApplicationContext applicationContext) {
        super(services, applicationContext, new ArrayList<>());
    }

    public WSFederationServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                       final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()), applicationContext);
    }

}
