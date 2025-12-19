package org.apereo.cas.configuration.model.support.ldap;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link FollowResultSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Accessors(chain = true)
@Setter

public class FollowResultSearchEntryHandlersProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7138108925310792763L;

    /**
     * The default referral limit.
     */
    private int limit = 10;
}
