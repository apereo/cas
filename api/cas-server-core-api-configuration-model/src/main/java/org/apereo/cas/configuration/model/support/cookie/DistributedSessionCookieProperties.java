package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * Cookie properties for Distributed Session.
 *
 * @author Travis Schmidt
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-core-cookie", automated = true)
@Getter
@Setter
public class DistributedSessionCookieProperties extends CookieProperties {

    public DistributedSessionCookieProperties() {
        super.setName("DISESSION");
        super.setPath("/cas/");
    }
}
