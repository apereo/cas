package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasEmbeddedApacheTomcatHttpProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
public class CasEmbeddedApacheTomcatHttpProperties implements Serializable {

    private static final long serialVersionUID = -8809922027350085888L;
    /**
     * Enable a separate port for the embedded container for HTTP access.
     */
    private boolean enabled;

    /**
     * The HTTP port to use.
     */
    private int port = 8080;

    /**
     * HTTP protocol to use.
     */
    private String protocol = "org.apache.coyote.http11.Http11NioProtocol";

    /**
     * Additional attributes to be set on the connector.
     */
    private Map<String, Object> attributes = new LinkedHashMap<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

}
