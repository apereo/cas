package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link YubiKeyMongoDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyMongoDbMultifactorProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = 6876845341227039713L;

    public YubiKeyMongoDbMultifactorProperties() {
        setCollection("MongoDbYubiKeyRepository");
    }

}
