package org.apereo.cas.configuration.model.support.ldap;

import java.io.Serializable;

/**
 * This is {@link DnAttributeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DnAttributeSearchEntryHandlersProperties implements Serializable {
    private static final long serialVersionUID = -1174594647679213858L;
    /**
     * The Dn attribute name.
     */
    private String dnAttributeName = "entryDN";
    /**
     * The Add if exists.
     */
    private boolean addIfExists;

    public String getDnAttributeName() {
        return dnAttributeName;
    }

    public void setDnAttributeName(final String dnAttributeName) {
        this.dnAttributeName = dnAttributeName;
    }

    public boolean isAddIfExists() {
        return addIfExists;
    }

    public void setAddIfExists(final boolean addIfExists) {
        this.addIfExists = addIfExists;
    }
}
