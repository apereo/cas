package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Getter
@EnabledIfPortOpen(port = 8000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DynamoDbCloudConfigBootstrapConfiguration.class
},
    properties = {
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint=http://localhost:8000",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance=true",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup=true",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key=test",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key=test"
    })
public class DynamoDbCloudConfigBootstrapConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", "http://localhost:8000");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "local-instance", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "prevent-table-creation-on-startup", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", "test");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", "test");

        val builder = new AmazonEnvironmentAwareClientBuilder(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);

        DynamoDbCloudConfigBootstrapConfiguration.createSettingsTable(amazonDynamoDBClient, true);

        val values = new HashMap<String, AttributeValue>();
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.ID.getColumnName(),
            AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.NAME.getColumnName(),
            AttributeValue.builder().s("cas.authn.accept.users").build());
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.VALUE.getColumnName(),
            AttributeValue.builder().s(STATIC_AUTHN_USERS).build());
        val request = PutItemRequest.builder().tableName(DynamoDbCloudConfigBootstrapConfiguration.TABLE_NAME).item(values).build();
        amazonDynamoDBClient.putItem(request);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
