package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.Map;
import java.util.Properties;

/**
 * This is {@link DynamoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "dynamoDbCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
@Slf4j
@Getter
public class DynamoDbCloudConfigBootstrapConfiguration implements PropertySourceLocator {

    /**
     * Configuration table name.
     */
    public static final String TABLE_NAME = "DynamoDbCasProperties";

    /**
     * Configuration prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.dynamoDb";

    private static final long PROVISIONED_THROUGHPUT = 10;

    @SneakyThrows
    public static void createSettingsTable(final DynamoDbClient amazonDynamoDBClient, final boolean deleteTables) {
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

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val props = new Properties();

        try {
            val builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            val amazonDynamoDBClient = builder.build(DynamoDbClient.builder(), DynamoDbClient.class);
            val preventTableCreationOnStartup = builder.getSetting("prevent-table-creation-on-startup", Boolean.class);
            if (!preventTableCreationOnStartup) {
                createSettingsTable(amazonDynamoDBClient, false);
            }
            val scan = ScanRequest.builder().tableName(TABLE_NAME).build();
            LOGGER.debug("Scanning table with request [{}]", scan);
            val result = amazonDynamoDBClient.scan(scan);
            LOGGER.debug("Scanned table with result [{}]", scan);

            result.items()
                .stream()
                .map(DynamoDbCloudConfigBootstrapConfiguration::retrieveSetting)
                .forEach(p -> props.put(p.getKey(), p.getValue()));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    private static Pair<String, Object> retrieveSetting(final Map<String, AttributeValue> entry) {
        val name = entry.get(ColumnNames.NAME.getColumnName()).s();
        val value = entry.get(ColumnNames.VALUE.getColumnName()).s();
        return Pair.of(name, value);
    }

    private static CreateTableRequest createCreateTableRequest() {
        val name = ColumnNames.ID.getColumnName();
        return CreateTableRequest.builder()
            .attributeDefinitions(AttributeDefinition.builder().attributeName(name).attributeType(ScalarAttributeType.S).build())
            .keySchema(KeySchemaElement.builder().attributeName(name).keyType(KeyType.HASH).build())
            .provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(PROVISIONED_THROUGHPUT)
                .writeCapacityUnits(PROVISIONED_THROUGHPUT).build())
            .tableName(TABLE_NAME)
            .build();
    }

    @Getter
    @RequiredArgsConstructor
    public enum ColumnNames {

        /**
         * Column id.
         */
        ID("id"),
        /**
         * Column name.
         */
        NAME("name"),
        /**
         * Column value.
         */
        VALUE("value");

        private final String columnName;
    }
}
