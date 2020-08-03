package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-gauth-mongo")
@Accessors(chain = true)
public class MongoDbGoogleAuthenticatorMultifactorProperties extends SingleCollectionMongoDbProperties {
    private static final long serialVersionUID = -200556119517414696L;

    /**
     * Collection name where tokens are kept to prevent replay attacks.
     */
    @RequiredProperty
    private String tokenCollection;

    public MongoDbGoogleAuthenticatorMultifactorProperties() {
        setCollection("MongoDbGoogleAuthenticatorRepository");
        setTokenCollection("MongoDbGoogleAuthenticatorTokenRepository");
    }
}

