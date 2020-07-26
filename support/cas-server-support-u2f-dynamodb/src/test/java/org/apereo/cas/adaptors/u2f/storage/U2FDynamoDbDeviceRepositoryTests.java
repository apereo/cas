package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FDynamoDbConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import software.amazon.awssdk.core.SdkSystemSetting;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FDynamoDbDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    U2FDynamoDbConfiguration.class,
    U2FConfiguration.class,
    CasCoreHttpConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.u2f.dynamo-db.endpoint=http://localhost:8000",
        "cas.authn.mfa.u2f.dynamo-db.drop-tables-on-startup=true",
        "cas.authn.mfa.u2f.dynamo-db.local-instance=true",
        "cas.authn.mfa.u2f.dynamo-db.region=us-east-1"
    })
@Tag("DynamoDb")
@EnabledIfPortOpen(port = 8000)
@Getter
public class U2FDynamoDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository deviceRepository;


    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }
}
