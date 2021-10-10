package org.apereo.cas.configuration.model.support.account.provision;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AccountManagementRegistrationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-account-mgmt")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AccountManagementRegistrationProvisioningProperties")
public class AccountManagementRegistrationProvisioningProperties implements Serializable {
    private static final long serialVersionUID = -1279683905942523034L;

    /**
     * Provision accounts via REST.
     */
    @NestedConfigurationProperty
    private RestfulAccountManagementRegistrationProvisioningProperties rest = new RestfulAccountManagementRegistrationProvisioningProperties();

    /**
     * Provision accounts via Groovy.
     */
    @NestedConfigurationProperty
    private GroovyAccountManagementRegistrationProvisioningProperties groovy = new GroovyAccountManagementRegistrationProvisioningProperties();

}
