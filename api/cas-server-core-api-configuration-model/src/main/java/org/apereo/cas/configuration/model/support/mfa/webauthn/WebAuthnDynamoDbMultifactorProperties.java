package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
public class WebAuthnDynamoDbMultifactorProperties extends AbstractDynamoDbProperties {
    @Serial
    private static final long serialVersionUID = -2261683393319585262L;

    /**
     * The table name used and created by CAS to hold records in DynamoDb.
     */
    private String tableName = "DynamoDbCasWebAuthnRecords";
}

