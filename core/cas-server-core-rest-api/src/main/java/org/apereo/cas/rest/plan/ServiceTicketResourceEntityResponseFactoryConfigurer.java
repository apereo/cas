package org.apereo.cas.rest.plan;
import module java.base;

/**
 * This is {@link ServiceTicketResourceEntityResponseFactoryConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface ServiceTicketResourceEntityResponseFactoryConfigurer {

    /**
     * Configure.
     *
     * @param plan the plan
     */
    void configureEntityResponseFactory(ServiceTicketResourceEntityResponseFactoryPlan plan);
}
