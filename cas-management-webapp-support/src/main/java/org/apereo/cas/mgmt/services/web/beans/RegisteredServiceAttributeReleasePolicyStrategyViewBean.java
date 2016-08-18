package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * The attribute release strategy used for views.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceAttributeReleasePolicyStrategyViewBean
        extends AbstractRegisteredServiceAttributeReleasePolicyStrategyBean implements Serializable {

    private static final long serialVersionUID = 6295156552393546070L;

    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
