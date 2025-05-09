package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdPDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-discovery")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPDiscoveryProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3547093517788229284L;

    /**
     * Locate discovery feed json file.
     */
    @NestedConfigurationProperty
    private List<SpringResourceProperties> resource = new ArrayList<>();

}
