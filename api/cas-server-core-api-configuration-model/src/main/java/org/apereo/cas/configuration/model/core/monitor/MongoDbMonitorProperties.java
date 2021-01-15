package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-mongo-monitor")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MongoDbMonitorProperties")
public class MongoDbMonitorProperties extends BaseMongoDbProperties {
    private static final long serialVersionUID = -1918436901491275547L;
}
