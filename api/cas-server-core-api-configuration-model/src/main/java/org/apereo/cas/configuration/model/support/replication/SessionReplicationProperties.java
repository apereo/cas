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
     * The name of the session cookie.
     *
     * When using a distributed session store specially backed by CAS to primarily replicate CAS tokens and tickets across a cluster of CAS servers,
     * the distribution mechanism needs to be made aware of session cookies. If a session is generated on a first node, when you reach the second node,
     * retrieving the session via the Tomcat session manager does not work. A {@code JSESSION_ID} cookie value is actually retrieved, but as there is no HTTP session
     * associated, it is discarded and a new session with a new identifier is created. The only way to deal with that is to directly read the cookie value.
     * The setting below allows one to customize the cookie name in such scenarios.
     */
    private String sessionCookieName = "JSESSIONID";
}
