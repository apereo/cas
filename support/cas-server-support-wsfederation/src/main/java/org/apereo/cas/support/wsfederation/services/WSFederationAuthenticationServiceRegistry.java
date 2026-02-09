package org.apereo.cas.support.wsfederation.services;

import module java.base;
import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link WSFederationAuthenticationServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class WSFederationAuthenticationServiceRegistry extends ImmutableInMemoryServiceRegistry {

    public WSFederationAuthenticationServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                                     final RegisteredService... services) {
        super(Arrays.stream(services).collect(Collectors.toList()), applicationContext, new ArrayList<>());
    }
}
