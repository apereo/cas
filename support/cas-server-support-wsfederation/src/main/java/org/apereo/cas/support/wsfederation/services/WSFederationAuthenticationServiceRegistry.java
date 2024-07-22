package org.apereo.cas.support.wsfederation.services;

import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link WSFederationAuthenticationServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class WSFederationAuthenticationServiceRegistry extends ImmutableInMemoryServiceRegistry {

    public WSFederationAuthenticationServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                                     final RegisteredService... services) {
        super(Arrays.stream(services).collect(Collectors.toList()), applicationContext, new ArrayList<>(0));
    }
}
