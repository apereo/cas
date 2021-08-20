package org.apereo.cas.logging;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DataAlreadyAcceptedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidSequenceTokenException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * This is {@link CloudWatchAppender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Plugin(name = "CloudWatchAppender", category = "Core", elementType = "appender", printObject = true)
@Slf4j
@SuppressWarnings("java:S2055")
public class CloudWatchAppender extends AbstractAppender implements Serializable {
    private static final long serialVersionUID = 1044758913028847477L;

    private static final int AWS_DRAIN_LIMIT = 256;

    private static final int AWS_LOG_STREAM_MAX_QUEUE_DEPTH = 10000;

    private static final int SHUTDOWN_TIMEOUT_MILLIS = 10000;

    private static final int AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS = 5;

    private final BlockingQueue<InputLogEvent> queue = new LinkedBlockingQueue<>(AWS_LOG_STREAM_MAX_QUEUE_DEPTH);

    private final Object monitor = new Object();

    private volatile boolean shutdown;

    private int flushPeriodMillis;

    private Thread deliveryThread;

    /**
     * Every PutLogEvents request must include the sequenceToken obtained from the response of the previous request.
     */
    private String sequenceTokenCache;

    private long lastReportedTimestamp = -1;

    private String logGroupName;

    private String logStreamName;

    private CloudWatchLogsClient awsLogsClient;

    private volatile boolean queueFull;

    private boolean createLogGroupIfNeeded;

    private boolean createLogStreamIfNeeded;

    /**
     * Create appender cloud watch appender.
     *
     * @param name                             the name
     * @param endpoint                         the endpoint
     * @param awsLogStreamName                 the aws log stream name
     * @param awsLogGroupName                  the aws log group name
     * @param awsLogStreamFlushPeriodInSeconds the aws log stream flush period in seconds
     * @param credentialAccessKey              the credential access key
     * @param credentialSecretKey              the credential secret key
     * @param awsLogRegionName                 the aws log region name
     * @param layout                           the layout
     * @param createIfNeeded                   whether to create the resources if needed. Default value is `true`. If
     *                                         either `createLogGroupIfNeeded` or `createLogStreamIfNeeded` is set, this
     *                                         will default to `false`.
     * @param createLogGroupIfNeeded           whether to create a Cloud Watch log group if needed. Default value is
     *                                         `false`. A value of `true takes precedence over a value of `false` for
     *                                         `createIfNeeded`
     * @param createLogStreamIfNeeded          whether to create a Cloud Watch log stream if needed. Default value is
     *                                         `false`. A value of `true takes precedence over a value of `false` for
     *                                         `createIfNeeded`
     */
    public CloudWatchAppender(final String name,
                              final String endpoint,
                              final String awsLogGroupName,
                              final String awsLogStreamName,
                              final String awsLogStreamFlushPeriodInSeconds,
                              final String credentialAccessKey,
                              final String credentialSecretKey,
                              final String awsLogRegionName,
                              final Layout<Serializable> layout,
                              final Boolean createIfNeeded,
                              final Boolean createLogGroupIfNeeded,
                              final Boolean createLogStreamIfNeeded) {
        this(name, awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, layout,
            createIfNeeded, createLogGroupIfNeeded, createLogStreamIfNeeded);

        try {
            LOGGER.debug("Connecting to AWS CloudWatch...");
            val builder = CloudWatchLogsClient.builder();
            if (StringUtils.isNotBlank(endpoint)) {
                builder.endpointOverride(new URI(endpoint));
            }
            builder.region(StringUtils.isBlank(awsLogRegionName) ? Region.AWS_GLOBAL : Region.of(awsLogRegionName));
            builder.credentialsProvider(ChainingAWSCredentialsProvider.getInstance(credentialAccessKey, credentialSecretKey));

            this.awsLogsClient = builder.build();
        } catch (final Exception e) {
            org.apereo.cas.util.LoggingUtils.error(LOGGER, e);
        }
    }

    /**
     * Create appender cloud watch appender.
     *
     * @param name                             the name
     * @param awsLogStreamName                 the aws log stream name
     * @param awsLogGroupName                  the aws log group name
     * @param awsLogStreamFlushPeriodInSeconds the aws log stream flush period in seconds
     * @param layout                           the layout
     * @param createIfNeeded                   whether to create the resources if needed. Default value is `true`. A
     *                                         value of `true` takes precedence over the other `create*IfNeeded`.
     * @param createLogGroupIfNeeded           whether to create a Cloud Watch log group if needed. Default value is
     *                                         `false`. A value of `true takes precedence over a value of `false` for
     *                                         `createIfNeeded`
     * @param createLogStreamIfNeeded          whether to create a Cloud Watch log stream if needed. Default value is
     *                                         `false`. A value of `true takes precedence over a value of `false` for
     *                                         `createIfNeeded`
     * @param awsLogs                          instance of AWSLogs to use
     */
    public CloudWatchAppender(final String name,
                              final String awsLogGroupName,
                              final String awsLogStreamName,
                              final String awsLogStreamFlushPeriodInSeconds,
                              final Layout<Serializable> layout,
                              final Boolean createIfNeeded,
                              final Boolean createLogGroupIfNeeded,
                              final Boolean createLogStreamIfNeeded,
                              final CloudWatchLogsClient awsLogs) {
        this(name, awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, layout,
            createIfNeeded, createLogGroupIfNeeded, createLogStreamIfNeeded);
        this.awsLogsClient = awsLogs;
    }

    private CloudWatchAppender(final String name,
                               final String awsLogGroupName,
                               final String awsLogStreamName,
                               final String awsLogStreamFlushPeriodInSeconds,
                               final Layout<Serializable> layout,
                               final Boolean createIfNeeded,
                               final Boolean createLogGroupIfNeeded,
                               final Boolean createLogStreamIfNeeded) {
        super(name, null, layout == null
            ? PatternLayout.createDefaultLayout()
            : layout, false, Property.EMPTY_ARRAY);

        var flushPeriod = AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS;
        if (awsLogStreamFlushPeriodInSeconds != null) {
            flushPeriod = Integer.parseInt(awsLogStreamFlushPeriodInSeconds);
        }
        flushPeriodMillis = flushPeriod * 1_000;

        this.logGroupName = awsLogGroupName;
        this.logStreamName = awsLogStreamName;

        if (createLogGroupIfNeeded == null && createLogStreamIfNeeded == null) {
            this.createLogGroupIfNeeded = Objects.requireNonNullElse(createIfNeeded, Boolean.TRUE);
            this.createLogStreamIfNeeded = Objects.requireNonNullElse(createIfNeeded, Boolean.TRUE);
        } else {
            this.createLogGroupIfNeeded = Objects.requireNonNullElse(createLogGroupIfNeeded, Boolean.FALSE);
            this.createLogStreamIfNeeded = Objects.requireNonNullElse(createLogStreamIfNeeded, Boolean.FALSE);
        }
    }

    /**
     * Create appender cloud watch appender.
     *
     * @param name                             the name
     * @param endpoint                         the endpoint
     * @param awsLogStreamName                 the aws log stream name
     * @param awsLogGroupName                  the aws log group name
     * @param awsLogStreamFlushPeriodInSeconds the aws log stream flush period in seconds
     * @param credentialAccessKey              the credential access key
     * @param credentialSecretKey              the credential secret key
     * @param awsLogRegionName                 the aws log region name
     * @param layout                           the layout
     * @param createIfNeeded                   whether to create resources if needed
     * @param createLogGroupIfNeeded           whether to create log group if needed
     * @param createLogStreamIfNeeded          whether to create log stream if needed
     * @return the cloud watch appender
     */
    @PluginFactory
    public static CloudWatchAppender createAppender(@PluginAttribute("name") final String name,
                                                    @PluginAttribute("endpoint") final String endpoint,
                                                    @PluginAttribute("awsLogStreamName") final String awsLogStreamName,
                                                    @PluginAttribute("awsLogGroupName") final String awsLogGroupName,
                                                    @PluginAttribute("awsLogStreamFlushPeriodInSeconds") final String awsLogStreamFlushPeriodInSeconds,
                                                    @PluginAttribute("credentialAccessKey") final String credentialAccessKey,
                                                    @PluginAttribute("credentialSecretKey") final String credentialSecretKey,
                                                    @PluginAttribute("awsLogRegionName") final String awsLogRegionName,
                                                    @PluginElement("Layout") final Layout<Serializable> layout,
                                                    @PluginAttribute(value = "createIfNeeded") final String createIfNeeded,
                                                    @PluginAttribute(value = "createLogGroupIfNeeded") final String createLogGroupIfNeeded,
                                                    @PluginAttribute(value = "createLogStreamIfNeeded") final String createLogStreamIfNeeded) {
        return new CloudWatchAppender(
            name,
            endpoint,
            awsLogGroupName,
            awsLogStreamName,
            awsLogStreamFlushPeriodInSeconds,
            StringUtils.defaultIfBlank(credentialAccessKey, System.getProperty("AWS_ACCESS_KEY")),
            StringUtils.defaultIfBlank(credentialSecretKey, System.getProperty("AWS_SECRET_KEY")),
            StringUtils.defaultIfBlank(awsLogRegionName, System.getProperty("AWS_REGION_NAME")),
            layout,
            StringUtils.isBlank(createIfNeeded) ? null : BooleanUtils.toBoolean(createIfNeeded),
            StringUtils.isBlank(createLogGroupIfNeeded) ? null : BooleanUtils.toBoolean(createLogGroupIfNeeded),
            StringUtils.isBlank(createLogStreamIfNeeded) ? null : BooleanUtils.toBoolean(createLogStreamIfNeeded));
    }

    @Override
    public void initialize() {
        this.sequenceTokenCache = createLogGroupAndLogStreamIfNeeded();
        super.initialize();
    }

    @Override
    public void append(final LogEvent logEvent) {
        val event = LoggingUtils.prepareLogEvent(logEvent);
        val message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        val timestamp = event.getTimeMillis();
        val awsLogEvent = InputLogEvent.builder().message(message).timestamp(timestamp).build();
        if (!queue.offer(awsLogEvent) && !queueFull) {
            queueFull = true;
        } else if (queueFull) {
            queueFull = false;
        }
    }

    @Override
    public void start() {
        super.start();
        this.deliveryThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    flush();
                } catch (final Exception e) {
                    org.apereo.cas.util.LoggingUtils.error(LOGGER, e);
                }
                if (!shutdown && queue.size() < AWS_DRAIN_LIMIT) {
                    try {
                        synchronized (monitor) {
                            monitor.wait(flushPeriodMillis);
                        }
                    } catch (final InterruptedException e) {
                        org.apereo.cas.util.LoggingUtils.error(LOGGER, e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            while (!queue.isEmpty()) {
                flush();
            }
        }, "CloudWatchAppenderDeliveryThread");
        if (StringUtils.isBlank(this.sequenceTokenCache)) {
            this.sequenceTokenCache = createLogGroupAndLogStreamIfNeeded();
        }
        deliveryThread.start();
    }

    @Override
    public void stop() {
        super.stop();
        shutdown = true;
        if (deliveryThread != null) {
            synchronized (monitor) {
                monitor.notifyAll();
            }
            try {
                deliveryThread.join(SHUTDOWN_TIMEOUT_MILLIS);
            } catch (final InterruptedException e) {
                deliveryThread.interrupt();
                org.apereo.cas.util.LoggingUtils.error(LOGGER, e);
            }
        }
        if (!queue.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        var drained = 0;
        var logEvents = new ArrayList<InputLogEvent>(AWS_DRAIN_LIMIT);
        do {
            drained = queue.drainTo(logEvents, AWS_DRAIN_LIMIT);
            if (logEvents.isEmpty()) {
                break;
            }
            logEvents.sort(Comparator.comparing(InputLogEvent::timestamp));
            if (lastReportedTimestamp > 0) {
                logEvents = logEvents.stream()
                    .map(event -> {
                        if (event.timestamp() < lastReportedTimestamp) {
                            return event.copy(builder -> builder.timestamp(lastReportedTimestamp));
                        }
                        return event;
                    })
                    .sorted(Comparator.comparing(InputLogEvent::timestamp))
                    .collect(Collectors.toCollection(ArrayList::new));
            }

            lastReportedTimestamp = logEvents.get(logEvents.size() - 1).timestamp();
            val putLogEventsRequest = PutLogEventsRequest.builder().logGroupName(logGroupName).logStreamName(logStreamName).logEvents(logEvents);
            if (StringUtils.isNotBlank(this.sequenceTokenCache)) {
                putLogEventsRequest.sequenceToken(this.sequenceTokenCache);
            }
            try {
                val putLogEventsResult = awsLogsClient.putLogEvents(putLogEventsRequest.build());
                sequenceTokenCache = putLogEventsResult.nextSequenceToken();
            } catch (final DataAlreadyAcceptedException daae) {
                sequenceTokenCache = daae.expectedSequenceToken();
            } catch (final InvalidSequenceTokenException iste) {
                sequenceTokenCache = iste.expectedSequenceToken();
            } catch (final Exception e) {
                org.apereo.cas.util.LoggingUtils.error(LOGGER, e);
            }
            logEvents.clear();
        } while (drained >= AWS_DRAIN_LIMIT);
    }

    private String createLogGroupAndLogStreamIfNeeded() {
        if (this.createLogGroupIfNeeded) {
            LOGGER.debug("Attempting to locate the log group [{}]", logGroupName);
            val describeLogGroupsResult = FunctionUtils.doAndHandle(
                () -> awsLogsClient.describeLogGroups(DescribeLogGroupsRequest.builder().logGroupNamePrefix(logGroupName).build()),
                throwable -> {
                    LOGGER.error(throwable.getMessage(), throwable);
                    return null;
                }).get();

            var createLogGroup = true;
            if (describeLogGroupsResult != null && describeLogGroupsResult.hasLogGroups()) {
                createLogGroup = describeLogGroupsResult.logGroups().stream().noneMatch(g -> g.logGroupName().equals(logGroupName));
            }
            if (createLogGroup) {
                try {
                    LOGGER.debug("Creating log group [{}]", logGroupName);
                    val createLogGroupRequest = CreateLogGroupRequest.builder().logGroupName(logStreamName).build();
                    awsLogsClient.createLogGroup(createLogGroupRequest);
                } catch (final Throwable throwable) {
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            }
        }

        var logSequenceToken = StringUtils.EMPTY;
        var createLogStream = true;
        LOGGER.debug("Attempting to locate the log stream [{}] for group [{}]", logStreamName, logGroupName);
        val describeLogStreamsRequest = DescribeLogStreamsRequest.builder().logGroupName(logGroupName).logStreamNamePrefix(logStreamName).build();
        val describeLogStreamsResult = FunctionUtils.doAndHandle(
            () -> awsLogsClient.describeLogStreams(describeLogStreamsRequest),
            throwable -> {
                LOGGER.error(throwable.getMessage(), throwable);
                return null;
            }).get();
        if (describeLogStreamsResult != null && describeLogStreamsResult.hasLogStreams()) {
            for (val ls : describeLogStreamsResult.logStreams()) {
                if (logStreamName.equals(ls.logStreamName())) {
                    createLogStream = false;
                    logSequenceToken = ls.uploadSequenceToken();
                    LOGGER.debug("Found log stream [{}] with sequence token [{}]", logStreamName, logSequenceToken);
                    break;
                }
            }
        }

        if (createLogStream) {
            if (!this.createLogStreamIfNeeded) {
                throw new RuntimeException("Log stream does not exist, yet `createIfNeeded` is false. This will not work");
            }
            try {
                LOGGER.debug("Creating log stream [{}] for group [{}]", logStreamName, logGroupName);
                val createLogStreamRequest = CreateLogStreamRequest.builder().logGroupName(logGroupName).logStreamName(logStreamName).build();
                awsLogsClient.createLogStream(createLogStreamRequest);
            } catch (final Throwable throwable) {
                LOGGER.error(throwable.getMessage(), throwable);
            }
        }
        if (StringUtils.isBlank(logSequenceToken)) {
            LOGGER.warn("Unable to determine CloudWatch log sequence token. Log stream [{}] "
                + "likely does not exist for log group [{}], or cannot be determined.", logStreamName, logGroupName);
        }
        return logSequenceToken;
    }
}
