package org.apereo.cas.services.domain;

/**
 * This is {@link RegisteredServiceDomainExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface RegisteredServiceDomainExtractor {
    /**
     * The default domain assigned to services.
     */
    String DOMAIN_DEFAULT = "default";

    /**
     * Extract domain.
     *
     * @param service the service
     * @return the string
     */
    String extract(String service);
}
