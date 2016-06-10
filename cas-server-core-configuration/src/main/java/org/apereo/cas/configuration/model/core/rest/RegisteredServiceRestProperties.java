package org.apereo.cas.configuration.model.core.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link RegisteredServiceRestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class RegisteredServiceRestProperties {
    private String attributeName;
    private String attributeValue;

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
}



