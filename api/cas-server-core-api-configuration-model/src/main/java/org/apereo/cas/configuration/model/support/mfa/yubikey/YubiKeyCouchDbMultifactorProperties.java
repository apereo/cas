package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link YubiKeyCouchDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 * @deprecated Since 7
 */
@RequiresModule(name = "cas-server-support-yubikey-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "7.0.0")
public class YubiKeyCouchDbMultifactorProperties extends BaseCouchDbProperties {
    @Serial
    private static final long serialVersionUID = 3757390989294642185L;

    public YubiKeyCouchDbMultifactorProperties() {
        this.setDbName("yubikey");
    }
}
