package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.syncope.BaseSyncopeSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link SyncopePasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class SyncopePasswordManagementProperties extends BaseSyncopeSearchProperties {
    @Serial
    private static final long serialVersionUID = -3772274510347618493L;
}
