package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link Pac4jDelegatedAuthenticationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-pac4j")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationProvisioningProperties")
public class Pac4jDelegatedAuthenticationProvisioningProperties implements Serializable {
    private static final long serialVersionUID = 3478567744591488495L;

    /**
     * Hand off the provisioning task to an external rest api
     * to create and manage establish profiles.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationRestfulProvisioningProperties rest = new Pac4jDelegatedAuthenticationRestfulProvisioningProperties();

    /**
     * Hand off the provisioning task to an external groovy script
     * to create and manage establish profiles.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationGroovyProvisioningProperties groovy = new Pac4jDelegatedAuthenticationGroovyProvisioningProperties();

}
