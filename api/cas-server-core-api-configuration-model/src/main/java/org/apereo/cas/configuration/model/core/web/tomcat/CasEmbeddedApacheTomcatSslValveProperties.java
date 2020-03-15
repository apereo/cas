package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatSslValveProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatSslValveProperties implements Serializable {

    private static final long serialVersionUID = 3164446071136700242L;

    /**
     * Enable the SSL valve for apache tomcat.
     */
    private boolean enabled;

    /**
     * Allows setting a custom name for the ssl_client_cert header.
     * If not specified, the default of ssl_client_cert is used.
     */
    private String sslClientCertHeader = "ssl_client_cert";

    /**
     * Allows setting a custom name for the ssl_cipher header.
     * If not specified, the default of ssl_cipher is used.
     */
    private String sslCipherHeader = "ssl_cipher";

    /**
     * Allows setting a custom name for the ssl_session_id header.
     * If not specified, the default of ssl_session_id is used.
     */
    private String sslSessionIdHeader = "ssl_session_id";

    /**
     * Allows setting a custom name for the ssl_cipher_usekeysize header.
     * If not specified, the default of ssl_cipher_usekeysize is used.
     */
    private String sslCipherUserKeySizeHeader = "ssl_cipher_usekeysize";
}
