package org.apereo.cas.configuration.model.support.account.provision;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link ScimAccountManagementRegistrationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-account-mgmt")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ScimAccountManagementRegistrationProvisioningProperties")
public class ScimAccountManagementRegistrationProvisioningProperties implements Serializable {
    private static final long serialVersionUID = 6833936824474022021L;
    
    /**
     * Whether provisioning to SCIM targets should be enabled
     * for delegated authentication attempts.
     */
    private boolean enabled;
}
