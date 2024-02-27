package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

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
