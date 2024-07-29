package org.apereo.cas.configuration.model.support.account.provision;

import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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

public class RestfulAccountManagementRegistrationProvisioningProperties extends BaseRestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 6855936824474022021L;
}
