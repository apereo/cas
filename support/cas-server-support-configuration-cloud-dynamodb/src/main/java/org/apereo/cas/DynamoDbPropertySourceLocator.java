package org.apereo.cas;

import module java.base;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * This is {@link DynamoDbPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
public class DynamoDbPropertySourceLocator implements PropertySourceLocator {

    /**
     * Configuration prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.dynamoDb";

    private static final long PROVISIONED_THROUGHPUT = 10;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = DynamoDbPropertySource.class.getSimpleName();
        val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
        val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);
        val preventTableCreationOnStartup = builder.getSetting("prevent-table-creation-on-startup", Boolean.class);
        if (Boolean.FALSE.equals(preventTableCreationOnStartup)) {
            createSettingsTable(amazonDynamoDBClient, false);
        }
        return new DynamoDbPropertySource(sourceName, amazonDynamoDBClient);
    }

    /**
     * Create settings table.
     *
     * @param amazonDynamoDBClient the amazon dynamo db client
     * @param deleteTables         the delete tables
     */
    public static void createSettingsTable(final DynamoDbClient amazonDynamoDBClient,
                                           final boolean deleteTables) {
        val request = createCreateTableRequest();
        if (deleteTables) {
            val delete = DeleteTableRequest.builder().tableName(request.tableName()).build();
            LOGGER.debug("delete create request [{}] to remove table if necessary", delete);
            DynamoDbTableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
        }
        LOGGER.debug("Sending create request [{}] to create table", request);
        DynamoDbTableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.tableName());
        DynamoDbTableUtils.waitUntilActive(amazonDynamoDBClient, request.tableName());
        val describeTableRequest = DescribeTableRequest.builder().tableName(request.tableName()).build();
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        val tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).table();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    private static CreateTableRequest createCreateTableRequest() {
        val name = DynamoDbColumnNames.NAME.getColumnName();
        return CreateTableRequest.builder()
            .attributeDefinitions(AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build())
            .keySchema(KeySchemaElement.builder().attributeName(name).keyType(KeyType.HASH).build())
            .provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(PROVISIONED_THROUGHPUT)
                .writeCapacityUnits(PROVISIONED_THROUGHPUT).build())
            .tableName(DynamoDbPropertySource.TABLE_NAME)
            .build();
    }
}
