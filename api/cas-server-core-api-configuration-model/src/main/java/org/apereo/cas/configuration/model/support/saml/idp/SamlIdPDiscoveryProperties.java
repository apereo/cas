package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
public class SamlIdPDiscoveryProperties implements Serializable {

    private static final long serialVersionUID = 3547093517788229284L;
    /**
     * Locate discovery feed json file.
     */
    @NestedConfigurationProperty
    private List<SpringResourceProperties> resource = new ArrayList<>(0);

}
