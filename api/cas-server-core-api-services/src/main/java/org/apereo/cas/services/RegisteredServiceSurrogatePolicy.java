package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link RegisteredServiceSurrogatePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceSurrogatePolicy extends Serializable {
    /**
     * Control whether surrogate authentication is enabled for this service.
     * @return true/false
     */
    boolean isEnabled();
}
