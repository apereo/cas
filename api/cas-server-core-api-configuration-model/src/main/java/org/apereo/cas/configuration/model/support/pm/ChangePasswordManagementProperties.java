package org.apereo.cas.configuration.model.support.pm;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link ChangePasswordManagementProperties}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class ChangePasswordManagementProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = 501430912571245280L;

    /**
     * Whether change operations require the current password.
     */
    private boolean currentPasswordRequired = true;
}
