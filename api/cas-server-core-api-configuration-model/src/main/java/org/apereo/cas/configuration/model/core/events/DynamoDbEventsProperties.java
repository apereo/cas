package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DynamoDbEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-events-dynamodb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("DynamoDbEventsProperties")
public class DynamoDbEventsProperties extends AbstractDynamoDbProperties {

    private static final long serialVersionUID = 612447148774854955L;

    /**
     * The table name used and created by CAS to hold events in DynamoDb.
     */
    private String tableName = "DynamoDbCasEvents";
}
