package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is the properties for the session replication.
 *
 * @author Jerome LELEU
 * @since 6.1.2
 */
@RequiresModule(name = "cas-server-support-pac4j-api")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SessionReplicationProperties")
public class SessionReplicationProperties implements Serializable {
    private static final long serialVersionUID = -3839399712674610962L;

    /**
     * Cookie setting for session replication.
     */
    @NestedConfigurationProperty
    private CookieSessionReplicationProperties cookie = new CookieSessionReplicationProperties();

}
