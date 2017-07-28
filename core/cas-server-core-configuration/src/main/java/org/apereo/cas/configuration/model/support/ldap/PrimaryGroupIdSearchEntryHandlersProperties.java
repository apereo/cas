package org.apereo.cas.configuration.model.support.ldap;

import java.io.Serializable;

/**
 * This is {@link PrimaryGroupIdSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class PrimaryGroupIdSearchEntryHandlersProperties implements Serializable {
    private String groupFilter = "(&(objectClass=group)(objectSid={0}))";
    private String baseDn;

    public String getGroupFilter() {
        return groupFilter;
    }

    public void setGroupFilter(final String groupFilter) {
        this.groupFilter = groupFilter;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }
}
