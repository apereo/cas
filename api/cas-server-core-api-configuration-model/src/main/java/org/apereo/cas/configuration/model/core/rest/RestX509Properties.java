package org.apereo.cas.configuration.model.core.rest;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RestX509Properties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest-x509")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RestX509Properties")
public class RestX509Properties implements Serializable {

    private static final long serialVersionUID = -1833117478273171342L;
    
    /**
     * Flag that enables {@link java.security.cert.X509Certificate}
     * extraction from the request headers
     * for authentication.
     */
    private boolean headerAuth = true;
    
    /**
     * Flag that enables {@link java.security.cert.X509Certificate}
     * extraction from the request body for authentication.
     */
    private boolean bodyAuth;

    /**
     * Flag that enables TLS client {@link java.security.cert.X509Certificate}
     * extraction from the servlet container for authentication.
     */
    private boolean tlsClientAuth;
}
