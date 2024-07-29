package org.apereo.cas.configuration.model.support.okta;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OktaPrincipalProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication")
@Getter
@Setter
@Accessors(chain = true)

public class OktaPrincipalProvisioningProperties extends BaseOktaApiProperties {

    @Serial
    private static final long serialVersionUID = 98007332402165L;

    /**
     * Whether or not provisioning should be enabled with Okta.
     */
    private boolean enabled;

    /**
     * Map of attributes that optionally may be used to control the names
     * of the attributes to Okta that form the user profile. If an attribute is provided by Okta, such as {@code department},
     * it can be listed here as the key of the map with a value that should be the name
     * of that attribute as collected and recorded by CAS.
     * For example, the convention {@code department->organization} will process the
     * CAS attribute {@code organization} and will assign its value to the user profile under {@code department}.
     * If no mapping is specified, the okta attribute itself will be used to find the CAS principal attribute value.
     */
    private Map<String, String> attributeMappings = new LinkedHashMap<>();
}
