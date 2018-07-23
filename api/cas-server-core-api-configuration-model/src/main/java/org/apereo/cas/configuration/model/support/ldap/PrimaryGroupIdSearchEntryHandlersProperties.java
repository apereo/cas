package org.apereo.cas.configuration.model.support.ldap;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link PrimaryGroupIdSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
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
}
