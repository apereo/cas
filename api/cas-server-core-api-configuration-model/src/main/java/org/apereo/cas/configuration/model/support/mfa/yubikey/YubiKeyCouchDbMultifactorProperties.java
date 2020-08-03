package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link YubiKeyCouchDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey-couchdb")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyCouchDbMultifactorProperties extends BaseCouchDbProperties {
    private static final long serialVersionUID = 3757390989294642185L;

    public YubiKeyCouchDbMultifactorProperties() {
        this.setDbName("yubikey");
    }
}
