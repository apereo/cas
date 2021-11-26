package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("MongoDbOidcJsonWebKeystoreProperties")
public class MongoDbOidcJsonWebKeystoreProperties extends SingleCollectionMongoDbProperties {
    private static final long serialVersionUID = -8392367146283877576L;
}
