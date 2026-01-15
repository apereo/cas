package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link RegisteredServicePasswordlessPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServicePasswordlessPolicy extends Serializable {

    /**
     * Control whether passwordless authentication is enabled for this service.
     * @return true/false
     */
    boolean isEnabled();
}
