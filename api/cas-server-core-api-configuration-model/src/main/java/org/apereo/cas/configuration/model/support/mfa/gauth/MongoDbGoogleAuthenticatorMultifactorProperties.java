package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

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

