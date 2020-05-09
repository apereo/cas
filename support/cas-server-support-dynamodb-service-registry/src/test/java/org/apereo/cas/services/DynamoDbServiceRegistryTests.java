package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.DynamoDbServiceRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link DynamoDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTest(classes = {
    DynamoDbServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class},
    properties = {
        "cas.service-registry.dynamoDb.endpoint=http://localhost:8000",
        "cas.service-registry.dynamoDb.dropTablesOnStartup=true",
        "cas.service-registry.dynamoDb.localInstance=true",
        "cas.service-registry.dynamoDb.region=us-east-1"
    })
@Tag("DynamoDb")
@EnabledIfPortOpen(port = 8000)
public class DynamoDbServiceRegistryTests extends AbstractServiceRegistryTests {

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry serviceRegistry;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
