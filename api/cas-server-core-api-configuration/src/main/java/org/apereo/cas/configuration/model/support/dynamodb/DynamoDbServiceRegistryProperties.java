package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link DynamoDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-service-registry")
public class DynamoDbServiceRegistryProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 809653348774854955L;

    /**
     * The table name used and created by CAS to hold service definitions in DynamoDb.
     */
    private String tableName = "DynamoDbCasServices";

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }
}
