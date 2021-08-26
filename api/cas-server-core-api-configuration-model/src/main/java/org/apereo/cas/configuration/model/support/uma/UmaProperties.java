package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

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
@JsonFilter("UmaProperties")
public class UmaProperties implements Serializable {
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
