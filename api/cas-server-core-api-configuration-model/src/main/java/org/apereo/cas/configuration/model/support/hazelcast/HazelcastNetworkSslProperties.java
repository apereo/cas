package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastNetworkSslProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastNetworkSslProperties")
public class HazelcastNetworkSslProperties implements Serializable {
    /**
     * Name of the algorithm which is used in
     * your TLS/SSL. For the protocol property, we recommend
     * you to provide TLS with its version information,
     * e.g., TLSv1.2. Note that if you write only TLS,
     * your application chooses the TLS version according to your Java version.
     */
    private String protocol = "TLS";

    /**
     * Path of your keystore file.
     * Only needed when the mutual authentication is used.
     */
    private String keystore;

    /**
     * Password to access the key from your keystore file.
     * Only needed when the mutual authentication is used.
     */
    private String keystorePassword;

    /**
     * Type of the keystore. Its default value is JKS.
     * Another commonly used type is the PKCS12. Available
     * keystore/truststore types depend on your Operating system and the Java runtime.
     * Only needed when the mutual authentication is used.
     */
    private String keyStoreType = "JKS";

    /**
     * Path of your truststore file. The file truststore
     * is a keystore file that contains a collection of
     * certificates trusted by your application.
     */
    private String trustStore;

    /**
     * Type of the truststore. Its default value is
     * JKS. Another commonly used type is the PKCS12.
     * Available keystore/truststore types depend on your
     * Operating system and the Java runtime.
     */
    private String trustStoreType = "JKS";

    /**
     * Password to unlock the truststore file.
     */
    private String trustStorePassword;

    /**
     * Mutual authentication configuration. It’s empty by
     * default which means the client side of connection is not authenticated.
     * Available values are:
     * <ul>
     * <li>{@code REQUIRED} - server forces usage of a trusted client certificate</li>
     * <li>{@code OPTIONAL} - server asks for a client certificate, but it doesn’t require it</li>
     * </ul>
     */
    private String mutualAuthentication;

    /**
     * Comma-separated list of cipher suite names allowed
     * to be used. Its default value are all supported suites in your Java runtime.
     */
    private String cipherSuites;

    /**
     * Name of the algorithm based on which the trust managers are provided.
     */
    private String trustManagerAlgorithm;

    /**
     * Name of the algorithm based on which the authentication keys are provided.
     */
    private String keyManagerAlgorithm;

    /**
     * Flag which allows enabling endpoint identity validation.
     * It means, during the TLS handshake client verifies if the
     * server’s hostname (or IP address) matches the information
     * in X.509 certificate (Subject Alternative Name extension).
     */
    private boolean validateIdentity;

}
