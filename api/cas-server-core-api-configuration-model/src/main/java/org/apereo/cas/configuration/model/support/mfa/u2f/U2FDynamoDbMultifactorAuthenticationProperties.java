package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FDynamoDbMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f-dynamodb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FDynamoDbMultifactorAuthenticationProperties")
public class U2FDynamoDbMultifactorAuthenticationProperties extends AbstractDynamoDbProperties {

    private static final long serialVersionUID = 612447148774854955L;

    /**
     * The table name used and created by CAS to hold devices in DynamoDb.
     */
    private String tableName = "DynamoDbU2FDevices";
}
