package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

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
