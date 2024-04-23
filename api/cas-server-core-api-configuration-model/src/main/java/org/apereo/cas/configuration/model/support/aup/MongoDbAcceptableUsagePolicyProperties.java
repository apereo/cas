package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
@JsonFilter("MongoDbAcceptableUsagePolicyProperties")
public class MongoDbAcceptableUsagePolicyProperties extends SingleCollectionMongoDbProperties {

    @Serial
    private static final long serialVersionUID = -1918436901491275547L;

    public MongoDbAcceptableUsagePolicyProperties() {
        setCollection("MongoDbCasAUPRepository");
    }
}
