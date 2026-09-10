package org.apereo.cas.logging;

import module java.base;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.status.StatusLogger;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@code CloudWatchAppenderSpecTests}
 * @author Jonathon Johnson
 * @since 6.2.0
 */
@Tag("AmazonWebServices")
class CloudWatchAppenderSpecTests {
    @ParameterizedTest
    @ValueSource(strings = {"0", "30"})
    void verifyShutdownWakesDeliveryAndDrainsQueue(final String flushPeriod) {
        val logs = mock(CloudWatchLogsClient.class);
        when(logs.describeLogStreams(any(DescribeLogStreamsRequest.class))).thenReturn(createDescribeLogStreamsResult());
        when(logs.putLogEvents(any(PutLogEventsRequest.class)))
            .thenReturn(PutLogEventsResponse.builder().nextSequenceToken("next").build());
        val appender = new CloudWatchAppender("shutdown-test", "test", "test", flushPeriod, null,
            false, false, false, logs);
        appender.initialize();
        appender.start();
        try {
            val deliveryThread = (Thread) Objects.requireNonNull(ReflectionTestUtils.getField(appender, "deliveryThread"));
            await().atMost(Duration.ofSeconds(5)).until(() ->
                deliveryThread.getState() == Thread.State.WAITING || deliveryThread.getState() == Thread.State.TIMED_WAITING);
            appender.append(Log4jLogEvent.newBuilder().setMessage(new SimpleMessage("pending log event")).build());

            assertTimeout(Duration.ofSeconds(5), (Executable) appender::stop);
            assertFalse(deliveryThread.isAlive());
            verify(logs).putLogEvents(argThat((PutLogEventsRequest request) -> request.logEvents().size() == 1
                && request.logEvents().getFirst().message().contains("pending log event")));
        } finally {
            appender.stop();
        }
    }

    @Test
    void verifyLookupFailuresUseStatusLogger() {
        val logs = mock(CloudWatchLogsClient.class);
        val groupFailure = new IllegalStateException("Log group lookup failed");
        val streamFailure = new IllegalStateException("Log stream lookup failed");
        when(logs.describeLogGroups(any(DescribeLogGroupsRequest.class))).thenThrow(groupFailure);
        when(logs.describeLogStreams(any(DescribeLogStreamsRequest.class))).thenThrow(streamFailure);

        val listener = mock(StatusListener.class);
        when(listener.getStatusLevel()).thenReturn(Level.ERROR);
        val statusLogger = StatusLogger.getLogger();
        statusLogger.registerListener(listener);
        try {
            val appender = new CloudWatchAppender("lookup-failure-test", "test", "test", "30", null,
                true, true, true, logs);
            assertDoesNotThrow(appender::initialize);
            verify(logs).createLogGroup(any(CreateLogGroupRequest.class));
            verify(logs).createLogStream(any(CreateLogStreamRequest.class));
            verify(listener).log(argThat(status -> Objects.equals(status.getThrowable(), groupFailure)));
            verify(listener).log(argThat(status -> Objects.equals(status.getThrowable(), streamFailure)));
        } finally {
            statusLogger.removeListener(listener);
        }
    }

    private static DescribeLogStreamsResponse createDescribeLogStreamsResult() {
        val logStream = LogStream.builder().logStreamName("test").uploadSequenceToken("test").build();
        return DescribeLogStreamsResponse.builder().logStreams(logStream).build();
    }

    private static DescribeLogGroupsResponse createDescribeLogGroupsResult() {
        val logGroup = LogGroup.builder().logGroupName("test").build();
        return DescribeLogGroupsResponse.builder().logGroups(logGroup).build();
    }

    private static void createLogGroup(final CloudWatchLogsClient logs, final Boolean value) {
        if (value) {
            verify(logs, atLeastOnce()).createLogGroup(ArgumentMatchers.any(CreateLogGroupRequest.class));
        } else {
            verify(logs, never()).createLogGroup(ArgumentMatchers.any(CreateLogGroupRequest.class));
        }
    }

    private static void createLogStream(final CloudWatchLogsClient logs, final Boolean value) {
        if (value) {
            verify(logs, atLeastOnce()).createLogStream(ArgumentMatchers.any(CreateLogStreamRequest.class));
        } else {
            verify(logs, never()).createLogStream(ArgumentMatchers.any(CreateLogStreamRequest.class));
        }
    }

    public static List<TestCase> generateTestCases() {
        val testCases = new ArrayList<TestCase>();

        testCases.add(new TestCase(null, null, null, true, true));
        testCases.add(new TestCase(null, null, false, false, false, true));
        testCases.add(new TestCase(null, null, true, false, true));
        testCases.add(new TestCase(null, false, null, false, false, true));
        testCases.add(new TestCase(null, false, false, false, false, true)); /* 5 */
        testCases.add(new TestCase(null, false, true, false, true));
        testCases.add(new TestCase(null, true, null, true, false, true));
        testCases.add(new TestCase(null, true, false, true, false, true));
        testCases.add(new TestCase(null, true, true, true, true));
        testCases.add(new TestCase(false, null, null, false, false, true)); /* 10 */
        testCases.add(new TestCase(false, null, false, false, false, true));
        testCases.add(new TestCase(false, null, true, false, true));
        testCases.add(new TestCase(false, false, null, false, false, true));
        testCases.add(new TestCase(false, false, false, false, false, true));
        testCases.add(new TestCase(false, false, true, false, true)); /* 15 */
        testCases.add(new TestCase(false, true, null, true, false, true));
        testCases.add(new TestCase(false, true, false, true, false, true));
        testCases.add(new TestCase(false, true, true, true, true));
        testCases.add(new TestCase(true, null, null, true, true));
        testCases.add(new TestCase(true, null, false, false, false, true)); /* 20 */
        testCases.add(new TestCase(true, null, true, false, true));
        testCases.add(new TestCase(true, false, null, false, false, true));
        testCases.add(new TestCase(true, false, false, false, false, true));
        testCases.add(new TestCase(true, false, true, false, true));
        testCases.add(new TestCase(true, true, null, true, false, true)); /* 25 */
        testCases.add(new TestCase(true, true, false, true, false, true));
        testCases.add(new TestCase(true, true, true, true, true));
        testCases.add(new TestCase(true, null, null, true, true, false, false, false));
        testCases.add(new TestCase(null, true, null, true, false, true, false, false));
        testCases.add(new TestCase(null, false, true, false, true, false, false, false)); /* 30 */
        testCases.add(new TestCase(null, true, true, true, true, false, false, false));

        return testCases;
    }

    @Test
    @DisplayName("make sure that log4j plugin file is generated")
    public void fileGenerated() {
        val pluginManager = new PluginManager("Core");
        pluginManager.collectPlugins();
        val plugin = pluginManager.getPluginType("cloudwatchappender");
        assertNotNull(plugin);
        assertEquals(CloudWatchAppender.class, plugin.getPluginClass());
    }

    @ParameterizedTest(name = "case {index}")
    @MethodSource("generateTestCases")
    @DisplayName("making sure incoming parameters are set correctly")
    void specTest(final TestCase tC) {
        var mock = mock(CloudWatchLogsClient.class);
        if (tC.logStreamExists) {
            when(mock.describeLogStreams(ArgumentMatchers.any(DescribeLogStreamsRequest.class))).thenReturn(createDescribeLogStreamsResult());
        }
        if (tC.logGroupExists) {
            when(mock.describeLogGroups(ArgumentMatchers.any(DescribeLogGroupsRequest.class))).thenReturn(createDescribeLogGroupsResult());
        }

        val appender = new CloudWatchAppender("test", "test", "test", "30", null,
            tC.createIfNeeded, tC.createLogGroupIfNeeded, tC.createLogStreamIfNeeded, mock);
        if (tC.throwsException) {
            assertThrows(RuntimeException.class, appender::initialize);
        } else {
            appender.initialize();
            createLogGroup(mock, Objects.requireNonNullElseGet(tC.resultCreateLogGroupIfNeeded,
                () -> Objects.requireNonNullElse(tC.createIfNeeded, true)));
            createLogStream(mock, Objects.requireNonNullElseGet(tC.resultCreateLogStreamIfNeeded,
                () -> Objects.requireNonNullElse(tC.createIfNeeded, true)));
        }

    }

    static class TestCase {
        private final @Nullable Boolean createIfNeeded;

        private final @Nullable Boolean createLogGroupIfNeeded;

        private final @Nullable Boolean createLogStreamIfNeeded;

        private final @Nullable Boolean resultCreateLogGroupIfNeeded;

        private final @Nullable Boolean resultCreateLogStreamIfNeeded;

        private final @Nullable Boolean logGroupExists;

        private final @Nullable Boolean logStreamExists;

        private final @Nullable Boolean throwsException;

        TestCase(
            @Nullable final Boolean createIfNeeded,
            @Nullable final Boolean createLogGroupIfNeeded,
            @Nullable final Boolean createLogStreamIfNeeded,
            @Nullable final Boolean resultCreateLogGroupIfNeeded,
            @Nullable final Boolean resultCreateLogStreamIfNeeded) {
            this(
                createIfNeeded,
                createLogGroupIfNeeded,
                createLogStreamIfNeeded,
                resultCreateLogGroupIfNeeded,
                resultCreateLogStreamIfNeeded,
                null,
                null,
                null);
        }

        TestCase(
            final @Nullable Boolean createIfNeeded,
            final @Nullable Boolean createLogGroupIfNeeded,
            final @Nullable Boolean createLogStreamIfNeeded,
            final @Nullable Boolean resultCreateLogGroupIfNeeded,
            final @Nullable Boolean resultCreateLogStreamIfNeeded,
            final @Nullable Boolean throwsException) {
            this(
                createIfNeeded,
                createLogGroupIfNeeded,
                createLogStreamIfNeeded,
                resultCreateLogGroupIfNeeded,
                resultCreateLogStreamIfNeeded,
                throwsException,
                null,
                null);
        }

        TestCase(
            final @Nullable Boolean createIfNeeded,
            final @Nullable Boolean createLogGroupIfNeeded,
            final @Nullable Boolean createLogStreamIfNeeded,
            final @Nullable Boolean resultCreateLogGroupIfNeeded,
            final @Nullable Boolean resultCreateLogStreamIfNeeded,
            final @Nullable Boolean throwsException,
            final @Nullable Boolean logGroupExists,
            final @Nullable Boolean logStreamExists) {
            this.createIfNeeded = createIfNeeded;
            this.createLogGroupIfNeeded = createLogGroupIfNeeded;
            this.createLogStreamIfNeeded = createLogStreamIfNeeded;
            this.resultCreateLogGroupIfNeeded = resultCreateLogGroupIfNeeded;
            this.resultCreateLogStreamIfNeeded = resultCreateLogStreamIfNeeded;
            this.throwsException = Objects.requireNonNullElse(throwsException, false);
            this.logGroupExists = Objects.requireNonNullElse(logGroupExists, false);
            this.logStreamExists = Objects.requireNonNullElse(logStreamExists, false);
        }
    }
}
