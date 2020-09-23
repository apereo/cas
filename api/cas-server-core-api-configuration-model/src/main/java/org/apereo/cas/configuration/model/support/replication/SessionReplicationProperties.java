package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
public class SessionReplicationProperties implements Serializable {
    private static final long serialVersionUID = -3839399712674610962L;

    /**
     * Cookie setting for session replication.
     */
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-pac4j-api", automated = true)
    public static class Cookie extends CookieProperties {
        private static final long serialVersionUID = 6165162204295764362L;

        /**
         * Decide if cookie paths should be automatically configured
         * based on the application context path, when the cookie
         * path is not configured.
         */
        private boolean autoConfigureCookiePath = true;

        public Cookie() {
            setName("DISSESSION");
        }
    }
}
