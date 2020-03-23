package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link PrimaryGroupIdSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
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
