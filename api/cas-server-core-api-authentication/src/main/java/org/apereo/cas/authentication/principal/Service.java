package org.apereo.cas.authentication.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marker interface for Services. Services are generally either remote
 * applications utilizing CAS or applications that principals wish to gain
 * access to. In most cases this will be some form of web application.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @since 3.0.0
 */
@FunctionalInterface
public interface Service extends Principal {
    Logger LOGGER = LoggerFactory.getLogger(Service.class);

    /**
     * Sets the principal.
     *
     * @param principal the new principal
     */
    default void setPrincipal(final String principal) {
    }

    /**
     * Get a string representation of service type. Each concrete type will return an appropriate string value if necessary.
     *
     * @return String representation of service type
     */
    default String getType() {
        return "simple";
    }
}
