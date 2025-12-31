package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

/**
 * This is {@link DynamoDbPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("NullAway.Init")
@Slf4j
public class DynamoDbPropertySource extends EnumerablePropertySource<DynamoDbClient>
    implements MutablePropertySource<DynamoDbClient> {

    /**
     * Configuration table name.
     */
    public static final String TABLE_NAME = "DynamoDbCasProperties";

    private final Set<String> propertyNames = new HashSet<>();

    public DynamoDbPropertySource(final String context, final DynamoDbClient dynamoDbClient) {
        super(context, dynamoDbClient);
        refresh();
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (propertyNames.contains(name)) {
            val keys = new HashMap<String, AttributeValue>();
            keys.put(DynamoDbColumnNames.NAME.getColumnName(), AttributeValue.builder().s(name).build());
            val request = GetItemRequest.builder().key(keys).tableName(TABLE_NAME).build();
            val returnItem = getSource().getItem(request).item();
            if (returnItem != null && !returnItem.isEmpty()) {
                val setting = retrieveSetting(returnItem);
                return setting.value();
            }
        }
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val values = new HashMap<String, AttributeValue>();
        values.put(DynamoDbColumnNames.NAME.getColumnName(),
            AttributeValue.builder().s(name).build());
        values.put(DynamoDbColumnNames.VALUE.getColumnName(),
            AttributeValue.builder().s(value.toString()).build());
        val request = PutItemRequest.builder()
            .tableName(DynamoDbPropertySource.TABLE_NAME)
            .item(values).build();
        getSource().putItem(request);
        propertyNames.add(name);
        return this;
    }

    @Override
    public void refresh() {
        val scan = ScanRequest.builder().tableName(TABLE_NAME).build();
        LOGGER.debug("Scanning table with request [{}]", scan);
        val result = getSource().scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        propertyNames.clear();
        propertyNames.addAll(result
            .items()
            .stream()
            .filter(entry -> entry.containsKey(DynamoDbColumnNames.VALUE.getColumnName())
                && entry.containsKey(DynamoDbColumnNames.NAME.getColumnName()))
            .map(DynamoDbPropertySource::retrieveSetting)
            .map(DymamoDbProperty::name)
            .toList());
    }

    @Override
    public void removeProperty(final String name) {
        val key = Map.of(DynamoDbColumnNames.NAME.getColumnName(), AttributeValue.builder().s(name).build());
        getSource().deleteItem(DeleteItemRequest.builder().tableName(TABLE_NAME).key(key).build());
        propertyNames.remove(name);
    }

    @Override
    public void removeAll() {
        val scan = ScanRequest.builder().tableName(TABLE_NAME).build();
        val result = getSource().scan(scan);
        result.items().forEach(item -> {
            val key = Map.of(DynamoDbColumnNames.NAME.getColumnName(),
                item.get(DynamoDbColumnNames.NAME.getColumnName()));
            getSource().deleteItem(DeleteItemRequest.builder().tableName(TABLE_NAME).key(key).build());
        });
        propertyNames.clear();
    }

    private static DymamoDbProperty retrieveSetting(final Map<String, AttributeValue> entry) {
        val name = Objects.requireNonNull(entry.get(DynamoDbColumnNames.NAME.getColumnName())).s();
        val value = Objects.requireNonNull(entry.get(DynamoDbColumnNames.VALUE.getColumnName())).s();
        return new DymamoDbProperty(name, value);
    }

    private record DymamoDbProperty(String name, Object value) {
    }

}
