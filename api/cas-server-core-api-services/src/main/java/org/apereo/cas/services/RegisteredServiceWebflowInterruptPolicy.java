package org.apereo.cas.services;

import org.apereo.cas.util.model.TriStateBoolean;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceWebflowInterruptPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceWebflowInterruptPolicy extends Serializable {

    /**
     * Whether webflow interrupt is enabled for this service.
     *
     * @return the boolean
     */
    boolean isEnabled();

    /**
     * Whether execution of the interrupt inquiry
     * query should be always forced, and the status
     * of interrupt check should be ignored.
     *
     * @return true/false/undefined
     */
    TriStateBoolean getForceExecution();
}
