package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa-mongo")

public class MongoDbTrustedDevicesMultifactorProperties extends SingleCollectionMongoDbProperties {

    @Serial
    private static final long serialVersionUID = 4940497540189318943L;

    public MongoDbTrustedDevicesMultifactorProperties() {
        setCollection("MongoDbCasTrustedAuthnMfaRepository");
    }
}
