package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link SyncopePrincipalProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)

public class SyncopePrincipalProvisioningProperties extends BaseSyncopeSearchProperties {

    @Serial
    private static final long serialVersionUID = 98447332402164L;

    /**
     * Syncope realm used for principal provisioning.
     * Realms define a hierarchical security domain tree, primarily meant for containing users.
     * The root realm contains everything, and other realms can be seen as containers that split
     * up the total number of entities into smaller pools.
     */
    @RequiredProperty
    private String realm = "/";

    /**
     * Whether or not provisioning should be enabled with Syncope.
     */
    private boolean enabled;
}
