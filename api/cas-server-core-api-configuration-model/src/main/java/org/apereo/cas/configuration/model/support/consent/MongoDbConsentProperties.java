package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbConsentProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -1918436901491275547L;

    public MongoDbConsentProperties() {
        setCollection("MongoDbCasConsentRepository");
    }
}
