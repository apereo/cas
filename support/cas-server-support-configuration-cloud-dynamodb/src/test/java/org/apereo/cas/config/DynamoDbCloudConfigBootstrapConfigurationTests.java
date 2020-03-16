package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
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
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "localInstance=true",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "preventTableCreationOnStartup=true",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialAccessKey=test",
        DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialSecretKey=test"
    })
public class DynamoDbCloudConfigBootstrapConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", "http://localhost:8000");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "localInstance", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "preventTableCreationOnStartup", "true");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialAccessKey", "test");
        environment.setProperty(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialSecretKey", "test");

        val builder = new AmazonEnvironmentAwareClientBuilder(DynamoDbCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(AmazonDynamoDBClient.builder(), AmazonDynamoDB.class);

        DynamoDbCloudConfigBootstrapConfiguration.createSettingsTable(amazonDynamoDBClient, true);
        
        val values = new HashMap<String, AttributeValue>();
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.ID.getColumnName(), new AttributeValue(UUID.randomUUID().toString()));
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.NAME.getColumnName(), new AttributeValue("cas.authn.accept.users"));
        values.put(DynamoDbCloudConfigBootstrapConfiguration.ColumnNames.VALUE.getColumnName(), new AttributeValue(STATIC_AUTHN_USERS));
        val request = new PutItemRequest(DynamoDbCloudConfigBootstrapConfiguration.TABLE_NAME, values);
        amazonDynamoDBClient.putItem(request);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
