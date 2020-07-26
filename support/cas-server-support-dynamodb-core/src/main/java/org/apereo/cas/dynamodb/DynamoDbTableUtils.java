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
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
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

    private static final int DEFAULT_WAIT_INTERVAL = 20 * 1000;

    /**
     * Wait until exists.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @param timeout   the timeout
     * @param interval  the interval
     * @throws InterruptedException the interrupted exception
     */
    public static void waitUntilExists(final DynamoDbClient dynamo, final String tableName, final int timeout,
                                       final int interval) throws InterruptedException {
        val table = waitForTableDescription(dynamo, tableName, null, timeout, interval);
        if (table == null) {
            throw SdkClientException.create("Table " + tableName + " never returned a result");
        }
    }

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @throws InterruptedException                   the interrupted exception
     * @throws TableNeverTransitionedToStateException the table never transitioned to state exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName)
        throws InterruptedException, TableNeverTransitionedToStateException {
        waitUntilActive(dynamo, tableName, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
    }

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @param timeout   the timeout
     * @param interval  the interval
     * @throws InterruptedException the interrupted exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName, final int timeout,
                                       final int interval) throws InterruptedException {
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
        } catch (final ResourceInUseException e) {
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
        throws InterruptedException, IllegalArgumentException {
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

    private static class TableNeverTransitionedToStateException extends SdkClientException {

        private static final long serialVersionUID = 8920567021104846647L;

        TableNeverTransitionedToStateException(final String tableName, final TableStatus desiredStatus) {
            super(TableNeverTransitionedToStateException
                .builder()
                .message("Table " + tableName + " never transitioned to desired state of " + desiredStatus.toString()));
        }

    }
}
