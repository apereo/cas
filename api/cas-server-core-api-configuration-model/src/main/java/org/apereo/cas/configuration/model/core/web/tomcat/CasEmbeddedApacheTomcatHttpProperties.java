package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Getter
@Setter
@Accessors(chain = true)
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
    private Map<String, String> attributes = new LinkedHashMap<>(0);
}
