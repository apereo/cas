package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is the properties for the session replication.
 *
 * @author Jerome LELEU
 * @since 6.1.2
 */
@RequiresModule(name = "cas-server-support-pac4j-api", automated = true)
@Getter
@Setter
public class SessionReplicationProperties implements Serializable {

    private static final long serialVersionUID = -3839399712674610962L;

    /**
     * The name of the specific session cookie used for replication.
     */
    private String sessionCookieName = "DISSESSION";
}
