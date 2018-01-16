package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * This is {@link PrimaryGroupIdSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class PrimaryGroupIdSearchEntryHandlersProperties implements Serializable {
    private static final long serialVersionUID = 539574118704476712L;
    /**
     * The Group filter.
     */
    private String groupFilter = "(&(objectClass=group)(objectSid={0}))";
    /**
     * The Base dn.
     */
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
