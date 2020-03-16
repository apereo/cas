package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatApachePortableRuntimeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatApachePortableRuntimeProperties implements Serializable {

    private static final long serialVersionUID = 8229851352067677264L;

    /**
     * Enable APR mode.
     */
    private boolean enabled;

    /**
     * SSL verify client.
     */
    private String sslProtocol;

    /**
     * SSL verify depth.
     */
    private int sslVerifyDepth = 10;

    /**
     * SSL verify client.
     */
    private String sslVerifyClient = "require";

    /**
     * SSL CA revocation file.
     */
    private File sslCaRevocationFile;

    /**
     * SSL certificate chain file.
     */
    private File sslCertificateChainFile;

    /**
     * SSL cipher suite.
     */
    private String sslCipherSuite;

    /**
     * Disable SSL compression.
     */
    private boolean sslDisableCompression;

    /**
     * Honor SSL cipher order.
     */
    private boolean sslHonorCipherOrder;

    /**
     * SSL password (if a cert is encrypted, and no password has
     * been provided, a callback will ask for a password).
     */
    private String sslPassword;

    /**
     * SSL CA certificate file.
     */
    private File sslCaCertificateFile;

    /**
     * SSL certificate key file.
     */
    private File sslCertificateKeyFile;

    /**
     * SSL certificate file.
     */
    private File sslCertificateFile;
}
