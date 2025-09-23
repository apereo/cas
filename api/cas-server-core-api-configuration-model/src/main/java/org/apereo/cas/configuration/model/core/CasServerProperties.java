package org.apereo.cas.configuration.model.core;

import org.apereo.cas.configuration.model.core.web.jetty.CasEmbeddedJettyProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
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

    @Serial
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
    @ExpressionLanguageCapable
    private String prefix;

    /**
     * The CAS Server scope.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String scope = "example.org";

    /**
     * Configuration settings that control the embedded Apache Tomcat container.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatProperties tomcat = new CasEmbeddedApacheTomcatProperties();

    /**
     * Configuration settings that control the embedded Jetty container.
     */
    @NestedConfigurationProperty
    private CasEmbeddedJettyProperties jetty = new CasEmbeddedJettyProperties();

    public CasServerProperties() {
        setPrefix(Strings.CI.appendIfMissing(getName(), "/").concat("cas"));
    }

    @JsonIgnore
    public String getLoginUrl() {
        return getPrefix().concat("/login");
    }

    @JsonIgnore
    public String getLogoutUrl() {
        return getPrefix().concat("/logout");
    }
}
