package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is the properties for the session replication.
 *
 * @author Jerome LELEU
 * @since 6.1.2
 */
@RequiresModule(name = "cas-server-support-pac4j-api", automated = true)
public class SessionReplicationProperties extends CookieProperties {

    private static final long serialVersionUID = -3839399712674610962L;

    public SessionReplicationProperties() {
        super.setName("DISSESSION");
        super.setPath("/cas/");
    }

    public void setSessionCookieName(final String name) {
        super.setName(name);
    }

    public String getSessionCookieName() {
        return super.getName();
    }

}
