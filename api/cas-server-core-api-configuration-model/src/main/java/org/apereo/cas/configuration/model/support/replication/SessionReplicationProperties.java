package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is the properties for the session replication.
 *
 * @author Jerome LELEU
 * @since 6.1.2
 */
@RequiresModule(name = "cas-server-core-api", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SessionReplicationProperties")
public class SessionReplicationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3839399712674610962L;

    /**
     * Indicates whether profiles and other session data,
     * collected as part of authentication flows and protocol requests
     * that are kept by the container session, should be replicated
     * across the cluster using CAS and its own ticket registry.
     * Without this option, profile data and other related
     * pieces of information should be manually replicated
     * via means and libraries outside of CAS.
     */
    private boolean replicateSessions = true;

    /**
     * Cookie setting for session replication.
     */
    @NestedConfigurationProperty
    private CookieSessionReplicationProperties cookie = new CookieSessionReplicationProperties();

}
