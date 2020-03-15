package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link LdapMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-core-ldap-monitor")
@Getter
@Setter
@Accessors(chain = true)
public class LdapMonitorProperties extends AbstractLdapProperties {
    private static final long serialVersionUID = 4722929378440179113L;

    /**
     * When monitoring the LDAP connection pool, indicates the amount of time the operation must wait
     * before it times outs and considers the pool in bad shape.
     */
    private String maxWait = "PT5S";

    /**
     * Whether LDAP monitoring should be enabled.
     */
    private boolean enabled = true;

    /**
     * Options that define the thread pool that will ping on the ldap pool.
     */
    @NestedConfigurationProperty
    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

    /**
     * Initialize minPoolSize for the monitor to zero.
     * This prevents a bad ldap connection from causing server to fail startup.
     * User can override this default via configuration.
     */
    public LdapMonitorProperties() {
        setMinPoolSize(0);
    }
}
