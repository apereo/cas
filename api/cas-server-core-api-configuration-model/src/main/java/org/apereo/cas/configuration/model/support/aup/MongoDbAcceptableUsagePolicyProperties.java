package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-mongo")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbAcceptableUsagePolicyProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -1918436901491275547L;

    public MongoDbAcceptableUsagePolicyProperties() {
        setCollection("MongoDbCasAUPRepository");
    }
}
