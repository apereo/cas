package org.apereo.cas.ws.idp.services;

import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link WSFederationServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WSFederationServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public WSFederationServiceRegistry(final List<RegisteredService> services,
                                       final ConfigurableApplicationContext applicationContext) {
        super(services, applicationContext, new ArrayList<>(0));
    }

    public WSFederationServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                       final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()), applicationContext);
    }

}
