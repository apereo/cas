package org.apereo.cas.configuration.model.support.replication;

import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CookieSessionReplicationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-pac4j-api")
@JsonFilter("CookieSessionReplicationProperties")
public class CookieSessionReplicationProperties extends PinnableCookieProperties {
    private static final long serialVersionUID = 6165162204295764362L;

    /**
     * Decide if cookie paths should be automatically configured
     * based on the application context path, when the cookie
     * path is not configured.
     */
    private boolean autoConfigureCookiePath = true;

    public CookieSessionReplicationProperties() {
        setName("DISSESSION");
    }
}
