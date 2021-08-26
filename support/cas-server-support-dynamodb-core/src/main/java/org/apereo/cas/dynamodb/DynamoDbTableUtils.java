package org.apereo.cas.dynamodb;

import org.apereo.cas.util.LoggingUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

/**
 * This is {@link DynamoDbTableUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
@Slf4j
public class DynamoDbTableUtils {
    private static final int DEFAULT_WAIT_TIMEOUT = 10 * 60 * 1000;

    private static final int DEFAULT_WAIT_INTERVAL = 10 * 1000;

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @throws Exception the exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName) throws Exception {
        waitUntilActive(dynamo, tableName, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
    }

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @param timeout   the timeout
     * @param interval  the interval
     * @throws Exception the exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName, final int timeout,
                                       final int interval) throws Exception {
        val table = waitForTableDescription(dynamo, tableName, TableStatus.ACTIVE, timeout, interval);

        if (table == null || !table.tableStatusAsString().equals(TableStatus.ACTIVE.toString())) {
            throw new TableNeverTransitionedToStateException(tableName, TableStatus.ACTIVE);
        }
    }

    /**
     * Creates the table and ignores any errors if it already exists.
     *
     * @param dynamo             The Dynamo client to use.
     * @param createTableRequest The create table request.
     * @return True if created, false otherwise.
     */
    public static boolean createTableIfNotExists(final DynamoDbClient dynamo, final CreateTableRequest createTableRequest) {
        try {
            dynamo.createTable(createTableRequest);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Deletes the table and ignores any errors if it doesn't exist.
     *
     * @param dynamo             The Dynamo client to use.
     * @param deleteTableRequest The delete table request.
     * @return True if deleted, false otherwise.
     */
    public static boolean deleteTableIfExists(final DynamoDbClient dynamo, final DeleteTableRequest deleteTableRequest) {
        try {
            dynamo.deleteTable(deleteTableRequest);
            return true;
        } catch (final ResourceNotFoundException e) {
            LOGGER.trace(e.getMessage());
        }
        return false;
    }

    private static TableDescription waitForTableDescription(final DynamoDbClient dynamo, final String tableName,
                                                            final TableStatus desiredStatus, final int timeout, final int interval)
        throws Exception {
        val startTime = System.currentTimeMillis();
        val endTime = startTime + timeout;

        TableDescription table = null;
        while (System.currentTimeMillis() < endTime) {
            try {
                table = dynamo.describeTable(DescribeTableRequest.builder().tableName(tableName).build()).table();
                if (desiredStatus == null || table.tableStatusAsString().equals(desiredStatus.toString())) {
                    return table;
                }
            } catch (final ResourceNotFoundException rnfe) {
                LOGGER.trace(rnfe.getMessage());
            }
            Thread.sleep(interval);
        }
        return table;
    }

    static class TableNeverTransitionedToStateException extends SdkClientException {

        private static final long serialVersionUID = 8920567021104846647L;

        /**
         * Instantiates a new Table never transitioned to state exception.
         *
         * @param tableName     the table name
         * @param desiredStatus the desired status
         */
        TableNeverTransitionedToStateException(final String tableName, final TableStatus desiredStatus) {
            super(TableNeverTransitionedToStateException
                .builder()
                .message("Table " + tableName + " never transitioned to desired state of " + desiredStatus.toString()));
        }

    }
}
