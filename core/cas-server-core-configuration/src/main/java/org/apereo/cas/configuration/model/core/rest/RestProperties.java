package org.apereo.cas.configuration.model.core.rest;

/**
 * This is {@link RestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestProperties {
    private String attributeName;
    private String attributeValue;
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



