package org.apereo.cas.configuration.model.core.rest;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link RestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest", automated = true)
public class RestProperties implements Serializable {
    private static final long serialVersionUID = -1833107478273171342L;
    /**
     * Authorization attribute name required by the REST endpoint in order to allow for the requested operation.
     * Attribute must be resolvable by the authenticated principal, or must have been already.
     */
    private String attributeName;
    /**
     * Matching authorization attribute value, pulled from the attribute
     * required by the REST endpoint in order to allow for the requested operation.
     */
    private String attributeValue;

    /**
     * The bean id of the throttler component whose job is to control rest authentication requests
     * an throttle requests per define policy.
     */
    private String throttler = "neverThrottle";
    
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(final String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getThrottler() {
        return throttler;
    }

    public void setThrottler(final String throttler) {
        this.throttler = throttler;
    }
}



