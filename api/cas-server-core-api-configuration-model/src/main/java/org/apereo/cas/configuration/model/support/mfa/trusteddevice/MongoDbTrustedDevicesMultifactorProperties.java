package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("MongoDbTrustedDevicesMultifactorProperties")
public class MongoDbTrustedDevicesMultifactorProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = 4940497540189318943L;

    public MongoDbTrustedDevicesMultifactorProperties() {
        setCollection("MongoDbCasTrustedAuthnMfaRepository");
    }
}
