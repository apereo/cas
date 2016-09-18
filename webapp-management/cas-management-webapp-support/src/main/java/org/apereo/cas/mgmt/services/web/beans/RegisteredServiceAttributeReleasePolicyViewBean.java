package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Attribute release policy defined per JSON feed.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceAttributeReleasePolicyViewBean extends AbstractRegisteredServiceAttributeReleasePolicyBean
        implements Serializable {
    private static final long serialVersionUID = -7567470297544895709L;

    private String attrPolicy;

    public String getAttrPolicy() {
        return this.attrPolicy;
    }

    public void setAttrPolicy(final String attrPolicy) {
        this.attrPolicy = attrPolicy;
    }
}
