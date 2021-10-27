package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * The attribute release strategy used for edits.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceAttributeReleasePolicyStrategyEditBean
        extends AbstractRegisteredServiceAttributeReleasePolicyStrategyBean implements Serializable {

    private static final long serialVersionUID = 6295156552393546070L;

    private String type = Types.ALLOWED.toString();
    private Object attributes;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Object getAttributes() {
        return attributes;
    }

    public void setAttributes(final Object attributes) {
        this.attributes = attributes;
    }
}
