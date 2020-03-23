package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DynamoDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class DynamoDbServiceRegistryProperties extends AbstractDynamoDbProperties {

    private static final long serialVersionUID = 809653348774854955L;

    /**
     * The table name used and created by CAS to hold service definitions in DynamoDb.
     */
    private String tableName = "DynamoDbCasServices";
}
