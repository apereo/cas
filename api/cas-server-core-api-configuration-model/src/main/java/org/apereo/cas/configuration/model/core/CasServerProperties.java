package org.apereo.cas.configuration.model.core;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)                                            
public class CasServerProperties implements Serializable {

    private static final long serialVersionUID = 7876382696803430817L;

    /**
     * Full name of the CAS server. This is the public-facing address
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
     * The CAS Server scope.
     */
    @RequiredProperty
    private String scope = "example.org";

    /**
     * Configuration settings that control the embedded Apache Tomcat container.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatProperties tomcat = new CasEmbeddedApacheTomcatProperties();

    @JsonIgnore
    public String getLoginUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    @JsonIgnore
    public String getLogoutUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGOUT);
    }

}
