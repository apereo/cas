package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.mock.env.MockEnvironment;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import java.util.HashMap;
import java.util.UUID;
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
    CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint=http://localhost:8000",
    CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance=true",
    CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup=true",
    CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key=test",
    CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key=test"
})
class DynamoDbCloudConfigBootstrapConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() throws Exception {
        val environment = new MockEnvironment();
        environment.setProperty(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", "http://localhost:8000");
        environment.setProperty(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance", "true");
        environment.setProperty(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup", "true");
        environment.setProperty(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", "test");
        environment.setProperty(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", "test");

        val builder = new AmazonEnvironmentAwareClientBuilder(CasDynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);

        CasDynamoDbCloudConfigBootstrapAutoConfiguration.createSettingsTable(amazonDynamoDBClient, true);
        val values = new HashMap<String, AttributeValue>();
        values.put(CasDynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.ID.getColumnName(),
            AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        values.put(CasDynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.NAME.getColumnName(),
            AttributeValue.builder().s("cas.authn.accept.users").build());
        values.put(CasDynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.VALUE.getColumnName(),
            AttributeValue.builder().s(STATIC_AUTHN_USERS).build());
        val request = PutItemRequest.builder().tableName(CasDynamoDbCloudConfigBootstrapAutoConfiguration.TABLE_NAME).item(values).build();
        amazonDynamoDBClient.putItem(request);
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
