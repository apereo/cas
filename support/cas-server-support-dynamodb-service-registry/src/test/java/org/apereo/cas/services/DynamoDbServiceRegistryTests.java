package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.DynamoDbCategory;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.DynamoDbServiceRegistryConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link DynamoDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    DynamoDbServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(locations = "classpath:/dynamodb-serviceregistry.properties")
@Slf4j
@Category(DynamoDbCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class DynamoDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry serviceRegistry;

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    public DynamoDbServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
