package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("DynamoDbConsentProperties")
public class DynamoDbConsentProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = -9012260892496773705L;

    /**
     * The table name used and created by CAS to hold consent records in DynamoDb.
     */
    private String tableName = "DynamoDbConsentRecords";
}
