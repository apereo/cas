package org.apereo.cas.configuration.model.support.account.provision;

import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulAccountManagementRegistrationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-account-mgmt")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RestfulAccountManagementRegistrationProvisioningProperties")
public class RestfulAccountManagementRegistrationProvisioningProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = 6855936824474022021L;
}
