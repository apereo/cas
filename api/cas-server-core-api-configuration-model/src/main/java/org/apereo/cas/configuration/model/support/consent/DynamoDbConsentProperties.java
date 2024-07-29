package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link DynamoDbConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-consent-dynamodb")
@Getter
@Setter
@Accessors(chain = true)

public class DynamoDbConsentProperties extends AbstractDynamoDbProperties {
    @Serial
    private static final long serialVersionUID = -9012260892496773705L;

    /**
     * The table name used and created by CAS to hold consent records in DynamoDb.
     */
    private String tableName = "DynamoDbConsentRecords";
}
