package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.DynamoDbColumnNames;
import org.apereo.cas.DynamoDbPropertySource;
import org.apereo.cas.DynamoDbPropertySourceLocator;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 8000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasDynamoDbCloudConfigBootstrapAutoConfiguration.class, properties = {
    DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".endpoint=http://localhost:8000",
    DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".local-instance=true",
    DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".prevent-table-creation-on-startup=true",
    DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".credential-access-key=test",
    DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".credential-secret-key=test"
})
class DynamoDbCloudConfigBootstrapConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableEnvironment environment;
    
    @BeforeAll
    public static void initialize() throws Exception {
        val environment = new MockEnvironment();
        environment.setProperty(getPropertyName("endpoint"), "http://localhost:8000");
        environment.setProperty(getPropertyName("local-instance"), "true");
        environment.setProperty(getPropertyName("prevent-table-creation-on-startup"), "true");
        environment.setProperty(getPropertyName("credential-access-key"), "test");
        environment.setProperty(getPropertyName("credential-secret-key"), "test");

        val builder = new AmazonEnvironmentAwareClientBuilder(DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);

        DynamoDbPropertySourceLocator.createSettingsTable(amazonDynamoDBClient, true);
        val values = new HashMap<String, AttributeValue>();
        values.put(DynamoDbColumnNames.NAME.getColumnName(),
            AttributeValue.builder().s("cas.authn.accept.users").build());
        values.put(DynamoDbColumnNames.VALUE.getColumnName(),
            AttributeValue.builder().s(STATIC_AUTHN_USERS).build());
        val request = PutItemRequest.builder().tableName(DynamoDbPropertySource.TABLE_NAME).item(values).build();
        amazonDynamoDBClient.putItem(request);
    }

    private static String getPropertyName(final String name) {
        return DynamoDbPropertySourceLocator.CAS_CONFIGURATION_PREFIX + '.' + name;
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());

        val propertySource = environment.getPropertySources()
            .stream()
            .filter(source -> source instanceof BootstrapPropertySource<?>)
            .map(BootstrapPropertySource.class::cast)
            .map(BootstrapPropertySource::getDelegate)
            .filter(MutablePropertySource.class::isInstance)
            .map(MutablePropertySource.class::cast)
            .findFirst()
            .orElseThrow();
        propertySource.setProperty("cas.server.prefix", "https://example.org/cas");
        val prefix = environment.getProperty("cas.server.prefix");
        assertEquals("https://example.org/cas", prefix);
        propertySource.removeProperty("cas.server.prefix");
        assertNull(environment.getProperty("cas.server.prefix"));
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }
}
