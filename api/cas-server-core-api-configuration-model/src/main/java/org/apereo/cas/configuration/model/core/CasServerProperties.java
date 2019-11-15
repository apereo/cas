package org.apereo.cas.configuration.model.core;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link CasServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
@Getter
@Setter
public class CasServerProperties implements Serializable {

    private static final long serialVersionUID = 7876382696803430817L;

    /**
     * Full name of the CAS server. This is public-facing address
     * of the CAS deployment and not the individual node address,
     * in the event that CAS is clustered.
     */
    @RequiredProperty
    private String name = "https://cas.example.org:8443";

    /**
     * A concatenation of the server name plus the CAS context path.
     * Deployments at root likely need to blank out this value.
     */
    @RequiredProperty
    private String prefix = name.concat("/cas");

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

    /**
     * Configuration settings that control the embedded Apache Tomcat container.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatProperties tomcat = new CasEmbeddedApacheTomcatProperties();

    public String getLoginUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    public String getLogoutUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGOUT);
    }

}
