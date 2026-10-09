package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.DynamoDbGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

/**
 * This is {@link DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator {
    /**
     * Global secondary index on the user id column. Lookups by user query this index instead of scanning the table.
     */
    public static final String USERID_INDEX_NAME = "useridIndex";

    private static final String EXPRESSION_NAME_USERID = "#userid";

    private static final String EXPRESSION_VALUE_USERID = ":userid";

    private final DynamoDbGoogleAuthenticatorMultifactorProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    private static GoogleAuthenticatorAccount extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
        val userId = item.get(ColumnNames.USERID.getColumnName()).s();
        val id = Long.parseLong(item.get(ColumnNames.ID.getColumnName()).n());
        val validationCode = Integer.parseInt(item.get(ColumnNames.VALIDATION_CODE.getColumnName()).n());
        val name = item.get(ColumnNames.NAME.getColumnName()).s();
        val secret = item.get(ColumnNames.SECRET.getColumnName()).s();
        val scratchCodes = item.containsKey(ColumnNames.SCRATCH_CODES.getColumnName())
            ? item.get(ColumnNames.SCRATCH_CODES.getColumnName()).ss()
            : List.<String>of();
        val properties = item.containsKey(ColumnNames.PROPERTIES.getColumnName())
            ? new ArrayList<>(item.get(ColumnNames.PROPERTIES.getColumnName()).ss())
            : new ArrayList<String>();
        val registrationTime = DateTimeUtils.zonedDateTimeOf(Long.parseLong(item.get(ColumnNames.REGISTRATION_DATE.getColumnName()).n()));
        val account = GoogleAuthenticatorAccount.builder()
            .id(id)
            .name(name)
            .registrationDate(registrationTime)
            .scratchCodes(scratchCodes.stream().map(BigInteger::new).collect(Collectors.<Number>toList()))
            .secretKey(secret)
            .username(userId)
            .validationCode(validationCode)
            .properties(properties)
            .build();
        if (item.containsKey(ColumnNames.TENANT.getColumnName())) {
            account.setTenant(item.get(ColumnNames.TENANT.getColumnName()).s());
        }
        if (item.containsKey(ColumnNames.LAST_USED_DATE_TIME.getColumnName())) {
            account.setLastUsedDateTime(item.get(ColumnNames.LAST_USED_DATE_TIME.getColumnName()).s());
        }
        return account;
    }

    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final OneTimeTokenAccount record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.NAME.getColumnName(), AttributeValue.builder().s(String.valueOf(record.getName())).build());
        values.put(ColumnNames.USERID.getColumnName(), AttributeValue.builder().s(record.getUsername().toLowerCase(Locale.ENGLISH)).build());
        values.put(ColumnNames.SECRET.getColumnName(), AttributeValue.builder().s(String.valueOf(record.getSecretKey())).build());
        if (!record.getScratchCodes().isEmpty()) {
            values.put(ColumnNames.SCRATCH_CODES.getColumnName(), AttributeValue.builder()
                .ss(record.getScratchCodes().stream().map(String::valueOf).collect(Collectors.toList())).build());
        }
        if (record.getTenant() != null && !record.getTenant().isBlank()) {
            values.put(ColumnNames.TENANT.getColumnName(), AttributeValue.builder().s(record.getTenant()).build());
        }
        if (record.getLastUsedDateTime() != null && !record.getLastUsedDateTime().isBlank()) {
            values.put(ColumnNames.LAST_USED_DATE_TIME.getColumnName(), AttributeValue.builder().s(record.getLastUsedDateTime()).build());
        }

        if (!record.getProperties().isEmpty()) {
            values.put(ColumnNames.PROPERTIES.getColumnName(), AttributeValue.builder().ss(record.getProperties()).build());
        }
        val time = record.getRegistrationDate().toInstant().toEpochMilli();
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(record.getId())).build());
        values.put(ColumnNames.REGISTRATION_DATE.getColumnName(),
            AttributeValue.builder().n(String.valueOf(time)).build());
        values.put(ColumnNames.VALIDATION_CODE.getColumnName(),
            AttributeValue.builder().n(String.valueOf(record.getValidationCode())).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Find the record by its identifier, which is the table's hash key, with a single {@code GetItem}.
     *
     * @param id the id
     * @return the one time token account, or null when there is none
     */
    public @Nullable OneTimeTokenAccount find(final long id) {
        val request = GetItemRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .key(keyOf(id))
            .build();
        val response = amazonDynamoDBClient.getItem(request);
        return response.hasItem() ? extractAttributeValuesFrom(response.item()) : null;
    }

    /**
     * Find the record by its identifier and return it only when it belongs to the given user.
     *
     * @param uid the username
     * @param id  the id
     * @return the one time token account, or null when the user has no such record
     */
    public @Nullable OneTimeTokenAccount find(final String uid, final long id) {
        return Optional.ofNullable(find(id))
            .filter(account -> account.getUsername().equals(normalizeUsername(uid)))
            .orElse(null);
    }

    /**
     * Find the user's records by querying the user id index.
     *
     * @param username the username
     * @return the list
     */
    public Collection<? extends OneTimeTokenAccount> find(final String username) {
        return amazonDynamoDBClient.queryPaginator(buildUserQuery(username).build())
            .items()
            .stream()
            .map(DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator::extractAttributeValuesFrom)
            .collect(Collectors.toList());
    }

    /**
     * Find all.
     *
     * @return the list
     */
    public Collection<? extends OneTimeTokenAccount> findAll() {
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                List.of(), DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator::extractAttributeValuesFrom)
            .collect(Collectors.toSet());
    }

    /**
     * Store.
     *
     * @param account the encoded account
     */
    public void store(final OneTimeTokenAccount account) {
        val values = buildTableAttributeValuesMap(account);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, account);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
    }

    /**
     * Remove all.
     */
    public void removeAll() {
        createTable(true);
    }

    /**
     * Remove the user's records, found through the user id index, one by one by key.
     *
     * @param username the username
     */
    public void remove(final String username) {
        find(username).forEach(record -> remove(record.getId()));
    }

    /**
     * Remove the record with the given identifier. The identifier is the table's hash key,
     * so the record is deleted by key and no other record is read or touched.
     *
     * @param id the id
     */
    public void remove(final long id) {
        val del = DeleteItemRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .key(keyOf(id))
            .build();
        LOGGER.debug("Submitting delete request [{}] for [{}]", del, id);
        val res = amazonDynamoDBClient.deleteItem(del);
        LOGGER.debug("Delete request came back with result [{}]", res);
    }

    /**
     * Count all records. The table is scanned for a count only, so no record is read back.
     *
     * @return the long
     */
    public long count() {
        val request = ScanRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .select(Select.COUNT)
            .build();
        return amazonDynamoDBClient.scanPaginator(request)
            .stream()
            .mapToLong(ScanResponse::count)
            .sum();
    }

    /**
     * Count the user's records by querying the user id index for a count only.
     *
     * @param username the username
     * @return the long
     */
    public long count(final String username) {
        return amazonDynamoDBClient.queryPaginator(buildUserQuery(username).select(Select.COUNT).build())
            .stream()
            .mapToLong(QueryResponse::count)
            .sum();
    }

    /**
     * Create the table keyed by the record id, with a global secondary index on the user id.
     * When the table already exists without the index, the index is added to it.
     *
     * @param deleteTables delete existing tables
     */
    public void createTable(final boolean deleteTables) {
        FunctionUtils.doUnchecked(_ -> DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables,
            List.of(
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.ID.getColumnName())
                    .attributeType(ScalarAttributeType.N).build(),
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.USERID.getColumnName())
                    .attributeType(ScalarAttributeType.S).build()),
            List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .keyType(KeyType.HASH).build()),
            List.of(buildUserIdIndex())));
    }

    /**
     * The column names.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ColumnNames {
        /**
         * User id column.
         */
        ID("id"),
        /**
         * User id column.
         */
        USERID("userid"),
        /**
         * secret column.
         */
        SECRET("secret"),
        /**
         * validation code column.
         */
        VALIDATION_CODE("validationCode"),
        /**
         * scratch code column.
         */
        SCRATCH_CODES("scratchCodes"),
        /**
         * properties column.
         */
        PROPERTIES("properties"),
        /**
         * registration time column.
         */
        REGISTRATION_DATE("registrationDate"),
        /**
         * name column.
         */
        NAME("name"),
        /**
         * tenant column.
         */
        TENANT("tenant"),
        /**
         * last used date/time column.
         */
        LAST_USED_DATE_TIME("lastUsedDateTime");

        private final String columnName;
    }

    private static String normalizeUsername(final String username) {
        return username.trim().toLowerCase(Locale.ENGLISH);
    }

    private static Map<String, AttributeValue> keyOf(final long id) {
        return CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(id)).build());
    }

    private QueryRequest.Builder buildUserQuery(final String username) {
        return QueryRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .indexName(USERID_INDEX_NAME)
            .keyConditionExpression(EXPRESSION_NAME_USERID + " = " + EXPRESSION_VALUE_USERID)
            .expressionAttributeNames(Map.of(EXPRESSION_NAME_USERID, ColumnNames.USERID.getColumnName()))
            .expressionAttributeValues(Map.of(EXPRESSION_VALUE_USERID, AttributeValue.builder().s(normalizeUsername(username)).build()));
    }

    private GlobalSecondaryIndex buildUserIdIndex() {
        val indexBuilder = GlobalSecondaryIndex.builder()
            .indexName(USERID_INDEX_NAME)
            .keySchema(KeySchemaElement.builder()
                .attributeName(ColumnNames.USERID.getColumnName())
                .keyType(KeyType.HASH)
                .build())
            .projection(Projection.builder().projectionType(ProjectionType.ALL).build());
        if (dynamoDbProperties.getBillingMode() == AbstractDynamoDbProperties.BillingMode.PROVISIONED) {
            indexBuilder.provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(dynamoDbProperties.getReadCapacity())
                .writeCapacityUnits(dynamoDbProperties.getWriteCapacity())
                .build());
        }
        return indexBuilder.build();
    }
}
