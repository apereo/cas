package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WebAuthnDynamoDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn-dynamodb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WebAuthnDynamoDbMultifactorProperties")
public class WebAuthnDynamoDbMultifactorProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = -2261683393319585262L;

    /**
     * The table name used and created by CAS to hold records in DynamoDb.
     */
    private String tableName = "DynamoDbCasWebAuthnRecords";
}

