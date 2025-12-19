package org.apereo.cas.services;
import module java.base;

/**
 * This is {@link RegisteredServicesTemplatesManager}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface RegisteredServicesTemplatesManager {
    /**
     * Bean implementation name.
     */
    String BEAN_NAME = "registeredServicesTemplatesManager";

    /**
     * Locate the template for the given service, if any,
     * and apply changes from the base definition.
     *
     * @param registeredService the registered service
     */
    RegisteredService apply(RegisteredService registeredService);

    /**
     * No op registered services templates manager.
     *
     * @return the registered services templates manager
     */
    static RegisteredServicesTemplatesManager noOp() {
        return service -> service;
    }
}
