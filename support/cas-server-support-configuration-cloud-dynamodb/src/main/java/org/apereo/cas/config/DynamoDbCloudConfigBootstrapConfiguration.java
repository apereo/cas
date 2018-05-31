package org.apereo.cas.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * This is {@link DynamoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbCloudConfigBootstrapConfiguration")
@Slf4j
@Getter
public class DynamoDbCloudConfigBootstrapConfiguration implements PropertySourceLocator {
    private static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.dynamodb";
    private static final String TABLE_NAME = "DynamoDbCasProperties";

    private static final long PROVISIONED_THROUGHPUT = 10;

    @Getter
    private enum ColumnNames {

        ID("id"), NAME("name"), VALUE("value");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final var props = new Properties();

        try {
            final var builder = new AmazonEnvironmentAwareClientBuilder(CAS_CONFIGURATION_PREFIX, environment);
            final var amazonDynamoDBClient = builder.build(AmazonDynamoDBClient.builder(), AmazonDynamoDB.class);
            final var preventTableCreationOnStartup = builder.getSetting("preventTableCreationOnStartup", Boolean.class);
            if (!preventTableCreationOnStartup) {
                createSettingsTable(amazonDynamoDBClient, false);
            }
            final var scan = new ScanRequest(TABLE_NAME);
            LOGGER.debug("Scanning table with request [{}]", scan);
            final var result = amazonDynamoDBClient.scan(scan);
            LOGGER.debug("Scanned table with result [{}]", scan);

            result.getItems()
                .stream()
                .map(DynamoDbCloudConfigBootstrapConfiguration::retrieveSetting)
                .forEach(p -> props.put(p.getKey(), p.getValue()));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new PropertiesPropertySource(getClass().getSimpleName(), props);
    }

    private static Pair<String, Object> retrieveSetting(final Map<String, AttributeValue> entry) {
        final var name = entry.get(ColumnNames.NAME.getColumnName()).getS();
        final var value = entry.get(ColumnNames.VALUE.getColumnName()).getS();
        return Pair.of(name, value);
    }

    @SneakyThrows
    private static void createSettingsTable(final AmazonDynamoDB amazonDynamoDBClient, final boolean deleteTables) {
        final var request = createCreateTableRequest();
        if (deleteTables) {
            final var delete = new DeleteTableRequest(request.getTableName());
            LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
            TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
        }
        LOGGER.debug("Sending delete request [{}] to create table", request);
        TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.getTableName());
        TableUtils.waitUntilActive(amazonDynamoDBClient, request.getTableName());
        final var describeTableRequest = new DescribeTableRequest().withTableName(request.getTableName());
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        final var tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).getTable();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private static CreateTableRequest createCreateTableRequest() {
        final var name = ColumnNames.ID.getColumnName();
        return new CreateTableRequest()
            .withAttributeDefinitions(new AttributeDefinition(name, ScalarAttributeType.S))
            .withKeySchema(new KeySchemaElement(name, KeyType.HASH))
            .withProvisionedThroughput(new ProvisionedThroughput(PROVISIONED_THROUGHPUT, PROVISIONED_THROUGHPUT))
            .withTableName(TABLE_NAME);
    }
}
