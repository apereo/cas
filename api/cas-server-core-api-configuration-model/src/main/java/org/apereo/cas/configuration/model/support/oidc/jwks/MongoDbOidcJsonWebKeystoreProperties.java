package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link MongoDbOidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbOidcJsonWebKeystoreProperties extends SingleCollectionMongoDbProperties {
    @Serial
    private static final long serialVersionUID = -8392367146283877576L;
}
