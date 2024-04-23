package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link MongoDbEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-events-mongo")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MongoDbEventsProperties")
public class MongoDbEventsProperties extends SingleCollectionMongoDbProperties {

    @Serial
    private static final long serialVersionUID = -1918436901491275547L;

    public MongoDbEventsProperties() {
        setCollection("MongoDbCasEventRepository");
    }
}
