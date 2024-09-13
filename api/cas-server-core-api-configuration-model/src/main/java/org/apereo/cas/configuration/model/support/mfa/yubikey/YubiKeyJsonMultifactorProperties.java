package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link YubiKeyJsonMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyJsonMultifactorProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = -4420099402220880362L;

    /**
     * A flag that determines whether the resource should be actively watched
     * for changes and updates. When set to true, the system will monitor
     * the resource for any modifications and reload it as necessary.
     */
    private boolean watchResource = true;
}
