package org.apereo.cas.configuration.model.support.geo.azure;

import org.apereo.cas.configuration.model.support.geo.BaseGeoLocationProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link AzureMapsProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-geolocation-azure")
@Getter
@Accessors(chain = true)
@Setter
public class AzureMapsProperties extends BaseGeoLocationProperties {

    @Serial
    private static final long serialVersionUID = 1665553818744933462L;

    /**
     * Directory (tenant) ID.
     */
    @ExpressionLanguageCapable
    private String tenantId;

    /**
     * Azure maps client id.
     */
    @ExpressionLanguageCapable
    private String clientId;

    /**
     * API key used for shared key authentication - This is
     * a simple and secure way for authenticating to the Azure Maps services.
     */
    @ExpressionLanguageCapable
    private String apiKey;
}
