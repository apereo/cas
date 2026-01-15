package org.apereo.cas.configuration.model.core.monitor;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
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
public class MongoDbMonitorProperties extends BaseMongoDbProperties {
    @Serial
    private static final long serialVersionUID = -1918436901491275547L;
}
