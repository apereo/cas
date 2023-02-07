package org.apereo.cas.dynamodb;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.util.HashMap;

/**
 * This is {@link DynamoDbHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DynamoDbHealthIndicator extends AbstractHealthIndicator {
    private final ApplicationContext applicationContext;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val entries = new HashMap<String, Object>();
        applicationContext.getBeansOfType(DynamoDbClient.class)
            .forEach((key, factory) -> factory.listTables().tableNames().forEach(tableName -> {
                if (!entries.containsKey(tableName)) {
                    var table = factory.describeTable(DescribeTableRequest.builder().tableName(tableName).build()).table();
                    builder.status(table.tableStatus() == TableStatus.ACTIVE ? Status.UP : Status.DOWN);
                    val details = CollectionUtils.wrap(
                        "status", table.tableStatusAsString(),
                        "creationDateTime", table.creationDateTime(),
                        "itemCount", table.itemCount(),
                        "tableSizeInBytes", table.tableSizeBytes(),
                        "tableArn", table.tableArn());
                    FunctionUtils.doIfNotNull(table.billingModeSummary(), summary -> details.put("billingMode", summary.billingModeAsString()));
                    FunctionUtils.doIfNotNull(table.provisionedThroughput(), tp -> details.put("readCapacity", tp.readCapacityUnits()));
                    FunctionUtils.doIfNotNull(table.provisionedThroughput(), tp -> details.put("writeCapacity", tp.writeCapacityUnits()));
                    entries.put(tableName, details);
                }
            }));
        builder.withDetails(entries);
    }
}
