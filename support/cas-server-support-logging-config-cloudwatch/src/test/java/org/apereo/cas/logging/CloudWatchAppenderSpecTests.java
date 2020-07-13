package org.apereo.cas.logging;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.logs.model.LogStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@code CloudWatchAppenderSpecTests}
 * @author Jonathon Johnson
 * @since 6.2.0
 */
@Tag("AmazonWebServices")
public class CloudWatchAppenderSpecTests {
    private static DescribeLogStreamsResult createDescribeLogStreamsResult() {
        var stream = new DescribeLogStreamsResult();
        var logStream = new LogStream();
        logStream.setLogStreamName("test");
        logStream.setUploadSequenceToken("test");
        stream.getLogStreams().add(logStream);

        return stream;
    }

    private static DescribeLogGroupsResult createDescribeLogGroupsResult() {
        var group = new DescribeLogGroupsResult();
        var logGroup = new LogGroup();
        logGroup.setLogGroupName("test");

        return group;
    }

    private static void createLogGroup(final AWSLogs logs, final Boolean value) {
        if (value) {
            Mockito.verify(logs, Mockito.atLeastOnce()).createLogGroup(Mockito.any(CreateLogGroupRequest.class));
        } else {
            Mockito.verify(logs, Mockito.never()).createLogGroup(Mockito.any(CreateLogGroupRequest.class));
        }
    }

    private static void createLogStream(final AWSLogs logs, final Boolean value) {
        if (value) {
            Mockito.verify(logs, Mockito.atLeastOnce()).createLogStream(Mockito.any(CreateLogStreamRequest.class));
        } else {
            Mockito.verify(logs, Mockito.never()).createLogStream(Mockito.any(CreateLogStreamRequest.class));
        }
    }

    public static ArrayList<TestCase> generateTestCases() {
        ArrayList<TestCase> testCases = new ArrayList<>();

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
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.add(builder.newAppender("cloudwatch", "CloudWatchAppender"));
        var configuration = builder.build();
        Configurator.initialize(configuration);
        assertNotNull(configuration.getAppender("cloudwatch"));
    }

    @ParameterizedTest(name = "case {index}")
    @MethodSource("generateTestCases")
    @DisplayName("making sure incoming parameters are set correctly")
    void specTest(final TestCase tC) {
        var mock = Mockito.mock(AWSLogs.class);
        if (tC.logStreamExists) {
            when(mock.describeLogStreams(Mockito.any(DescribeLogStreamsRequest.class))).thenReturn(createDescribeLogStreamsResult());
        }
        if (tC.logGroupExists) {
            when(mock.describeLogGroups(Mockito.any(DescribeLogGroupsRequest.class))).thenReturn(createDescribeLogGroupsResult());
        }

        /*
          we do this because the lifecycle is a little different for this sort of programmatic configuration
          not allowing single line comments is lame
         */
        var appender = new CloudWatchAppender("test", "test", "test", "30", null, tC.createIfNeeded, tC.createLogGroupIfNeeded, tC.createLogStreamIfNeeded, mock);
        if (tC.throwsException) {
            Assertions.assertThrows(RuntimeException.class, appender::initialize);
        } else {
            appender.initialize();

            ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
            var configuration = builder.build();
            configuration.addAppender(appender);
            Configurator.initialize(configuration);

            var logger = LogManager.getLogger("test");
            logger.info("here is a message");

            createLogGroup(mock, Objects.requireNonNullElse(tC.resultCreateLogGroupIfNeeded, Objects.requireNonNullElse(tC.createIfNeeded, true)));
            createLogStream(mock, Objects.requireNonNullElse(tC.resultCreateLogStreamIfNeeded, Objects.requireNonNullElse(tC.createIfNeeded, true)));
        }

    }

    private static class TestCase {
        private final Boolean createIfNeeded;

        private final Boolean createLogGroupIfNeeded;

        private final Boolean createLogStreamIfNeeded;

        private final Boolean resultCreateLogGroupIfNeeded;

        private final Boolean resultCreateLogStreamIfNeeded;

        private final Boolean logGroupExists;

        private final Boolean logStreamExists;

        private final Boolean throwsException;

        TestCase(
            final Boolean createIfNeeded,
            final Boolean createLogGroupIfNeeded,
            final Boolean createLogStreamIfNeeded,
            final Boolean resultCreateLogGroupIfNeeded,
            final Boolean resultCreateLogStreamIfNeeded) {
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
            final Boolean createIfNeeded,
            final Boolean createLogGroupIfNeeded,
            final Boolean createLogStreamIfNeeded,
            final Boolean resultCreateLogGroupIfNeeded,
            final Boolean resultCreateLogStreamIfNeeded,
            final Boolean throwsException) {
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
            final Boolean createIfNeeded,
            final Boolean createLogGroupIfNeeded,
            final Boolean createLogStreamIfNeeded,
            final Boolean resultCreateLogGroupIfNeeded,
            final Boolean resultCreateLogStreamIfNeeded,
            final Boolean throwsException,
            final Boolean logGroupExists,
            final Boolean logStreamExists) {
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
