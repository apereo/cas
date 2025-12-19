package org.apereo.cas.configuration.model.core.web.jetty;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CasEmbeddedJettyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-webapp-jetty")
@Getter
@Accessors(chain = true)
@Setter

public class CasEmbeddedJettyProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3630008224249750029L;

    /**
     * Server Name Indication is an extension of the Transport Layer Security (TLS) protocol,
     * which allows a client to indicate which hostname it is attempting to connect to at the
     * start of the handshaking process. This is particularly useful when a server
     * hosts multiple domains with different SSL certificates on a single IP address. Setting this setting to {@code false}
     * would mean that the Jetty server will not strictly require clients to send an SNI extension during the SSL/TLS handshake
     * and disables host name checking for Server Name Indication (SNI) during SSL/TLS handshakes.
     */
    private boolean sniHostCheck = true;
}
