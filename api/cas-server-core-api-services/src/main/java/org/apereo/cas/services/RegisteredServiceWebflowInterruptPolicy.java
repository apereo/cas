package org.apereo.cas.services;

import org.apereo.cas.configuration.support.TriStateBoolean;
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
     * @return true/false
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

    /**
     * Gets principal attribute name that must exist
     * before interrupt can be triggered for this service.
     *
     * @return the attribute name
     */
    String getAttributeName();

    /**
     * Gets principal attribute value that must exist
     * before interrupt can be triggered for this service.
     *
     * @return the attribute value
     */
    String getAttributeValue();

    /**
     * Gets inline/external groovy script
     * to determine if interrupt should be activated for the service.
     *
     * @return the groovy script
     */
    String getGroovyScript();
}
