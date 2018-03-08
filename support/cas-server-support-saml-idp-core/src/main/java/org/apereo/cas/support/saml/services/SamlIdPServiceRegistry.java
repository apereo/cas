package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link SamlIdPServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SamlIdPServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public SamlIdPServiceRegistry(final List<RegisteredService> services) {
        super(services);
    }

    public SamlIdPServiceRegistry(final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()));
    }

}
