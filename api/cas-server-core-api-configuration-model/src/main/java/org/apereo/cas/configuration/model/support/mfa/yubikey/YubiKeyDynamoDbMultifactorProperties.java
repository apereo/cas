package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link YubiKeyDynamoDbMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey-dynamodb")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyDynamoDbMultifactorProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 321667148774858855L;

    /**
     * The table name used and created by CAS to hold devices in DynamoDb.
     */
    private String tableName = "DynamoDbYubiKeyDevices";
}
