package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasEmbeddedApacheSslHostConfigProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Accessors(chain = true)
@Setter
@JsonFilter("CasEmbeddedApacheSslHostConfigProperties")
public class CasEmbeddedApacheSslHostConfigProperties implements Serializable {

    private static final long serialVersionUID = -32143821503580896L;

    /**
     * Enable this host config.
     */
    @RequiredProperty
    private boolean enabled;

    /**
     * Should the JSSE provider enable certificate revocation checks? This attribute is intended to enable
     * revocation checks that have been configured for the current JSSE provider via other means.
     * If not specified, a default of false is used.
     */
    private boolean revocationEnabled;

    /**
     * Name of the file that contains the concatenated certificates for
     * the trusted certificate authorities. The format is PEM-encoded.
     */
    private String caCertificateFile;

    /**
     * Set to required if you want the SSL stack to require a valid certificate chain from
     * the client before accepting a connection. Set to optional if you want the SSL stack to
     * request a client Certificate, but not fail if one isn't presented. Set to optionalNoCA
     * if you want client certificates to be optional and you don't want Tomcat to check them
     * against the list of trusted CAs. If the TLS provider doesn't support this option
     * (OpenSSL does, JSSE does not) it is treated as if optional was specified. A none value
     * (which is the default) will not require a certificate chain unless the client requests
     * a resource protected by a security constraint that uses CLIENT-CERT authentication.
     */
    private String certificateVerification = "require";

    /**
     * The name of the SSL Host. This should either be the fully qualified domain name
     * (e.g. tomcat.apache.org) or a wild card domain name (e.g. *.apache.org). If not
     * specified, the default value of _default_ will be used.
     */
    private String hostName;

    /**
     * The SSL protocol(s) to use (a single value may enable multiple protocols - see the JVM
     * documentation for details). If not specified, the default is TLS. The permitted values
     * may be obtained from the JVM documentation for the allowed values for
     * algorithm when creating an SSLContext instance
     */
    private String sslProtocol = "TLS";
    
    /**
     * OpenSSL only.
     * Configures if insecure renegotiation is allowed. The default is false.
     * If the OpenSSL version used does not support configuring if insecure renegotiation
     * is allowed then the default for that OpenSSL version will be used.
     */
    private boolean insecureRenegotiation;

    /**
     * The maximum number of intermediate certificates that will be allowed when validating
     * client certificates. If not specified, the default value of 10 will be used.
     */
    private int certificateVerificationDepth = 10;

    /**
     * The names of the protocols to support when communicating with clients. This should be
     * a list of any combination of the following:
     *
     * <ul>
     * <li>SSLv2Hello</li>
     * <li>SSLv3</li>
     * <li>TLSv1</li>
     * <li>TLSv1.1</li>
     * <li>TLSv1.2</li>
     * <li>TLSv1.3</li>
     * <li>all</li>
     * </ul>
     * Each token in the list can be prefixed with a plus sign ("+") or a minus sign ("-").
     * A plus sign adds the protocol, a minus sign removes it form the current list.
     * The list is built starting from an empty list.
     *
     * The token all is an alias for SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2,TLSv1.3.
     *
     * Note that TLSv1.3 is only supported for JSSE when using a JVM that implements TLSv1.3.
     *
     * Note that SSLv2Hello will be ignored for OpenSSL based secure connectors. If more
     * than one protocol is specified for an OpenSSL based secure connector it will always
     * support SSLv2Hello. If a single protocol is specified it will not support SSLv2Hello.
     *
     * Note that SSLv2 and SSLv3 are inherently unsafe.
     *
     * If not specified, the default value of all will be used.
     */
    private String protocols = "all";
    
    /**
     * List of certificates managed by the ssl host config.
     */
    private List<CasEmbeddedApacheSslHostConfigCertificateProperties> certificates = new ArrayList<>();

}
