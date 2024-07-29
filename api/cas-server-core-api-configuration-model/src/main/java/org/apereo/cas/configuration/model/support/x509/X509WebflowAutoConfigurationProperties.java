package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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

public class X509WebflowAutoConfigurationProperties extends WebflowAutoConfigurationProperties {
    @Serial
    private static final long serialVersionUID = 2744305877450488111L;

    /**
     * Default order for webflow configuration.
     */
    private static final int DEFAULT_ORDER = 80;

    /**
     * Port that is used to enact x509 client authentication
     * as a separate connector. Configuration of a separate server connector
     * and port allows the separation of client-auth functionality
     * from the rest of the server, allowing for opt-in behavior.
     * <p>
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
        setOrder(DEFAULT_ORDER);
    }
}
