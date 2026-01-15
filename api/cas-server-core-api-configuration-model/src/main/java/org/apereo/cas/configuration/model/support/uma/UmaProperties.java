package org.apereo.cas.configuration.model.support.uma;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link UmaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Accessors(chain = true)
@Setter

public class UmaProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 865028615694269276L;

    /**
     * Handles core settings.
     */
    @NestedConfigurationProperty
    private UmaCoreProperties core = new UmaCoreProperties();

    /**
     * Handles settings related to permission tickets.
     */
    @NestedConfigurationProperty
    private UmaPermissionTicketProperties permissionTicket = new UmaPermissionTicketProperties();

    /**
     * Handles settings related to rpt tokens.
     */
    @NestedConfigurationProperty
    private UmaRequestingPartyTokenProperties requestingPartyToken = new UmaRequestingPartyTokenProperties();

    /**
     * Handles settings related to management of resource-sets, etc.
     */
    @NestedConfigurationProperty
    private UmaResourceSetProperties resourceSet = new UmaResourceSetProperties();

}
