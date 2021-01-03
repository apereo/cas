package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link WsFederationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-ws-idp")
@JsonFilter("WsFederationProperties")
public class WsFederationProperties implements Serializable {

    private static final long serialVersionUID = -8679379856243224647L;

    /**
     * Settings related to the wed-fed identity provider.
     */
    @NestedConfigurationProperty
    private WsFederationIdentityProviderProperties idp = new WsFederationIdentityProviderProperties();

    /**
     * Settings related to the we-fed security token service.
     */
    @NestedConfigurationProperty
    private WsFederationSecurityTokenServiceProperties sts = new WsFederationSecurityTokenServiceProperties();

}
