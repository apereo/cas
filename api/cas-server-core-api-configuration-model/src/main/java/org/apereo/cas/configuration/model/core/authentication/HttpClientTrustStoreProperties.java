package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.security.KeyStore;

/**
 * This is {@link HttpClientTrustStoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class HttpClientTrustStoreProperties implements Serializable {
    private static final long serialVersionUID = -1357168622083627654L;

    /**
     * The CAS local truststore resource to contain certificates to the CAS deployment.
     * In the event that local certificates are to be imported into the CAS running environment,
     * a local truststore is provided by CAS to improve portability of configuration across environments.
     */
    private transient Resource file;

    /**
     * The truststore password.
     */
    private String psw = "changeit";

    /**
     * Truststore type used to create a SSL context for http client.
     */
    private String type = KeyStore.getDefaultType();
}
