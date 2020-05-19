package org.apereo.cas.consent.services;

import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ConsentServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConsentServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public ConsentServiceRegistry(final List<RegisteredService> services, final ConfigurableApplicationContext applicationContext) {
        super(services, applicationContext, new ArrayList<>(0));
    }

    public ConsentServiceRegistry(final ConfigurableApplicationContext applicationContext, final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()), applicationContext);
    }

}
