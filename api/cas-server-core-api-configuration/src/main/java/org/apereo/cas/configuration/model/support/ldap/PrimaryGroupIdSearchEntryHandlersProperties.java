package org.apereo.cas.configuration.model.support.ldap;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link PrimaryGroupIdSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
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
