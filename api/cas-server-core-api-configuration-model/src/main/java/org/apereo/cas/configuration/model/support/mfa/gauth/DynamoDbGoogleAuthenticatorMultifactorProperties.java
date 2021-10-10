package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DynamoDbGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-gauth-dynamodb")
@Accessors(chain = true)
@JsonFilter("DynamoDbGoogleAuthenticatorMultifactorProperties")
public class DynamoDbGoogleAuthenticatorMultifactorProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = -1161683393319585262L;

    /**
     * The table name used and created by CAS to hold accounts in DynamoDb.
     */
    private String tableName = "DynamoDbGoogleAuthenticatorRepository";

    /**
     * The table name used and created by CAS to hold tokens in DynamoDb.
     */
    private String tokenTableName = "DynamoDbGoogleAuthenticatorTokenRepository";
}
