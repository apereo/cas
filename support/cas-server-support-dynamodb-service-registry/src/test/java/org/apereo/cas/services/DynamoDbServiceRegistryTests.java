package org.apereo.cas.services;

import org.apereo.cas.config.CasDynamoDbServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;

/**
 * This is {@link DynamoDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTest(classes = {
    CasDynamoDbServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.service-registry.dynamo-db.endpoint=http://localhost:8000",
        "cas.service-registry.dynamo-db.drop-tables-on-startup=true",
        "cas.service-registry.dynamo-db.local-instance=true",
        "cas.service-registry.dynamo-db.region=us-east-1"
    })
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 8000)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DynamoDbServiceRegistryTests extends AbstractServiceRegistryTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry newServiceRegistry;
}
