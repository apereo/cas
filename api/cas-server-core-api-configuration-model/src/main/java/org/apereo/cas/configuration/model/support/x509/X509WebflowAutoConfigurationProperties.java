package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link X509WebflowAutoConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-x509-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("X509WebflowAutoConfigurationProperties")
public class X509WebflowAutoConfigurationProperties extends WebflowAutoConfigurationProperties {
    private static final long serialVersionUID = 2744305877450488111L;

    /**
     * Port that is used to enact x509 client authentication
     * as a separate connector. Configuration of a separate server connector
     * and port allows the separation of client-auth functionality
     * from the rest of the server, allowing for opt-in behavior.
     * 
     * To activate, a non-zero port must be specified.
     */
    @RequiredProperty
    private int port;

    /**
     * Indicate the strategy that should be used to
     * enforce client x509 authentication.
     * Accepted values are {@code true, false, want}.
     */
    private String clientAuth = "want";

    public X509WebflowAutoConfigurationProperties() {
        setOrder(10);
    }
}
