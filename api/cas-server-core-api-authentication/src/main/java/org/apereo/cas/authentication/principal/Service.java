package org.apereo.cas.authentication.principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
    default void setPrincipal(String principal) {}

    /**
     * Whether the services matches another.
     *
     * @param service the service
     * @return true, if successful
     */
    default boolean matches(Service service) {
        try {
            final String thisUrl = URLDecoder.decode(getId(), StandardCharsets.UTF_8.name());
            final String serviceUrl = URLDecoder.decode(service.getId(), StandardCharsets.UTF_8.name());

            LOGGER.trace("Decoded urls and comparing [{}] with [{}]", thisUrl, serviceUrl);
            return thisUrl.equalsIgnoreCase(serviceUrl);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
