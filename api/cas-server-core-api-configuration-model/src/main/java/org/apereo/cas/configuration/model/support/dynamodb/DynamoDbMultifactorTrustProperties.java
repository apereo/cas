package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DynamoDbMultifactorTrustProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class DynamoDbMultifactorTrustProperties extends AbstractDynamoDbProperties {

    private static final long serialVersionUID = 102540148774854955L;

    /**
     * The table name used and created by CAS to hold mfa trust definitions in DynamoDb.
     */
    private String tableName = "DynamoDbCasMfaTrustRecords";
}
