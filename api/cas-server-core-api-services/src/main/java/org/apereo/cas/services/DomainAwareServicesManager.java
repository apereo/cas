package org.apereo.cas.services;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * This is {@link ChainingServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface DomainAwareServicesManager {
    /**
     * Returns a list of domains being managed by the ServiceManager.
     *
     * @return list of domain names
     */
    default Stream<String> getDomains() {
        return Stream.of("default");
    }

    /**
     * Return a list of services for the passed domain.
     *
     * @param domain the domain name
     * @return list of services
     */
    Collection<RegisteredService> getServicesForDomain(String domain);
}
