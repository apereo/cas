package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.AbstractYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.config.CasDynamoDbYubiKeyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasDynamoDbYubiKeyAutoConfiguration.class,
    BaseYubiKeyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.yubikey.dynamo-db.endpoint=http://localhost:8000",
        "cas.authn.mfa.yubikey.dynamo-db.drop-tables-on-startup=true",
        "cas.authn.mfa.yubikey.dynamo-db.local-instance=true",
        "cas.authn.mfa.yubikey.dynamo-db.region=us-east-1",
        "cas.authn.mfa.yubikey.dynamo-db.asynchronous=false",
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 8000)
@Getter
class DynamoDbYubiKeyAccountRegistryTests extends AbstractYubiKeyAccountRegistryTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;

    @Autowired
    @Qualifier("yubikeyAccountCipherExecutor")
    private CipherExecutor yubikeyAccountCipherExecutor;


}
