package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatSslValveProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getSslClientCertHeader() {
        return sslClientCertHeader;
    }

    public void setSslClientCertHeader(final String sslClientCertHeader) {
        this.sslClientCertHeader = sslClientCertHeader;
    }

    public String getSslCipherHeader() {
        return sslCipherHeader;
    }

    public void setSslCipherHeader(final String sslCipherHeader) {
        this.sslCipherHeader = sslCipherHeader;
    }

    public String getSslSessionIdHeader() {
        return sslSessionIdHeader;
    }

    public void setSslSessionIdHeader(final String sslSessionIdHeader) {
        this.sslSessionIdHeader = sslSessionIdHeader;
    }

    public String getSslCipherUserKeySizeHeader() {
        return sslCipherUserKeySizeHeader;
    }

    public void setSslCipherUserKeySizeHeader(final String sslCipherUserKeySizeHeader) {
        this.sslCipherUserKeySizeHeader = sslCipherUserKeySizeHeader;
    }

}
