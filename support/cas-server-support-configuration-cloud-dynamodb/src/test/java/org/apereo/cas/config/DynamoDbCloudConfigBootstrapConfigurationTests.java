package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    DynamoDbCloudConfigBootstrapAutoConfiguration.class
},
    properties = {
        DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint=http://localhost:8000",
        DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance=true",
        DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup=true",
        DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key=test",
        DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key=test"
    })
class DynamoDbCloudConfigBootstrapConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() throws Exception {
        val environment = new MockEnvironment();
        environment.setProperty(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", "http://localhost:8000");
        environment.setProperty(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", "test");
        environment.setProperty(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", "test");

        val builder = new AmazonEnvironmentAwareClientBuilder(DynamoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);

        DynamoDbCloudConfigBootstrapAutoConfiguration.createSettingsTable(amazonDynamoDBClient, true);
        val values = new HashMap<String, AttributeValue>();
        values.put(DynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.ID.getColumnName(),
            AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        values.put(DynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.NAME.getColumnName(),
            AttributeValue.builder().s("cas.authn.accept.users").build());
        values.put(DynamoDbCloudConfigBootstrapAutoConfiguration.ColumnNames.VALUE.getColumnName(),
            AttributeValue.builder().s(STATIC_AUTHN_USERS).build());
        val request = PutItemRequest.builder().tableName(DynamoDbCloudConfigBootstrapAutoConfiguration.TABLE_NAME).item(values).build();
        amazonDynamoDBClient.putItem(request);
    }

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
