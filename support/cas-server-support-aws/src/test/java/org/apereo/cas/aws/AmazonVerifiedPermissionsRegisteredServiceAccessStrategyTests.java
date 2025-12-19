package org.apereo.cas.aws;

import module java.base;
import org.apereo.cas.aws.authz.AmazonVerifiedPermissionsRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import software.amazon.awssdk.core.SdkSystemSetting;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonVerifiedPermissionsRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("AmazonWebServices")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class AmazonVerifiedPermissionsRegisteredServiceAccessStrategyTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AmazonVerifiedPermissionsRegisteredServiceAccessStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() {
        val strategy = new AmazonVerifiedPermissionsRegisteredServiceAccessStrategy();
        strategy.setActionId("read");
        strategy.setCredentialAccessKey(UUID.randomUUID().toString());
        strategy.setCredentialSecretKey(UUID.randomUUID().toString());
        strategy.setPolicyStoreId("123456");
        strategy.setRegion("us-east-1");
        strategy.setContext(CollectionUtils.wrap("key1", "value1"));
        MAPPER.writeValue(JSON_FILE, strategy);
        val read = MAPPER.readValue(JSON_FILE, AmazonVerifiedPermissionsRegisteredServiceAccessStrategy.class);
        assertEquals(strategy, read);
    }

    @Test
    void verifyOperation() {
        val strategy = new AmazonVerifiedPermissionsRegisteredServiceAccessStrategy();
        strategy.setActionId("read");

        val accessRequest = RegisteredServiceAccessStrategyRequest.builder()
            .principalId("casuser")
            .applicationContext(applicationContext)
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .attributes(CollectionUtils.wrap("key1", Set.of("value1")))
            .build();
        assertFalse(strategy.authorizeRequest(accessRequest));
    }

}
