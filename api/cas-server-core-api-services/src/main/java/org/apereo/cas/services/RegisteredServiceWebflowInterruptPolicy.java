package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceWebflowInterruptPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceWebflowInterruptPolicy extends Serializable {

    /**
     * Whether webflow interrupt is enabled for this service.
     *
     * @return the boolean
     */
    boolean isEnabled();
}
