package org.apereo.cas.logging;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;

import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DataAlreadyAcceptedException;
import com.amazonaws.services.logs.model.DescribeLogGroupsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.InvalidSequenceTokenException;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private AWSLogs awsLogsClient;

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
        this(name, awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, layout, createIfNeeded, createLogGroupIfNeeded, createLogStreamIfNeeded);

        try {
            LOGGER.debug("Connecting to AWS CloudWatch...");
            val builder = AWSLogsClient.builder();
            if (StringUtils.isNotBlank(endpoint)) {
                builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, awsLogRegionName));
            } else {
                builder.setRegion(awsLogRegionName);
            }
            builder.setCredentials(ChainingAWSCredentialsProvider.getInstance(credentialAccessKey, credentialSecretKey));

            this.awsLogsClient = builder.build();
        } catch (final SdkClientException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
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
                              final AWSLogs awsLogs) {
        this(name, awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, layout, createIfNeeded, createLogGroupIfNeeded, createLogStreamIfNeeded);
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
        super(name, null, layout == null ? PatternLayout.createDefaultLayout() : layout, false, Property.EMPTY_ARRAY);

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

    @Override
    public void initialize() {
        this.sequenceTokenCache = createLogGroupAndLogStreamIfNeeded();
        super.initialize();
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
                                                    @PluginAttribute(value = "createIfNeeded", defaultBoolean = true) final Boolean createIfNeeded,
                                                    @PluginAttribute(value = "createLogGroupIfNeeded") final Boolean createLogGroupIfNeeded,
                                                    @PluginAttribute(value = "createLogStreamIfNeeded") final Boolean createLogStreamIfNeeded) {
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
            createIfNeeded,
            createLogGroupIfNeeded,
            createLogStreamIfNeeded);
    }

    private void flush() {
        var drained = 0;
        val logEvents = new ArrayList<InputLogEvent>(AWS_DRAIN_LIMIT);
        do {
            drained = queue.drainTo(logEvents, AWS_DRAIN_LIMIT);
            if (logEvents.isEmpty()) {
                break;
            }
            logEvents.sort(Comparator.comparing(InputLogEvent::getTimestamp));
            if (lastReportedTimestamp > 0) {
                for (val event : logEvents) {
                    if (event.getTimestamp() < lastReportedTimestamp) {
                        event.setTimestamp(lastReportedTimestamp);
                    }
                }
            }

            lastReportedTimestamp = logEvents.get(logEvents.size() - 1).getTimestamp();
            val putLogEventsRequest = new PutLogEventsRequest(logGroupName, logStreamName, logEvents);
            if (StringUtils.isNotBlank(this.sequenceTokenCache)) {
                putLogEventsRequest.setSequenceToken(this.sequenceTokenCache);
            }
            try {
                val putLogEventsResult = awsLogsClient.putLogEvents(putLogEventsRequest);
                sequenceTokenCache = putLogEventsResult.getNextSequenceToken();
            } catch (final DataAlreadyAcceptedException daae) {
                sequenceTokenCache = daae.getExpectedSequenceToken();
            } catch (final InvalidSequenceTokenException iste) {
                sequenceTokenCache = iste.getExpectedSequenceToken();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            logEvents.clear();
        } while (drained >= AWS_DRAIN_LIMIT);
    }

    @Override
    public void append(final LogEvent logEvent) {
        val event = LoggingUtils.prepareLogEvent(logEvent);
        val awsLogEvent = new InputLogEvent();
        val timestamp = event.getTimeMillis();
        val message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        awsLogEvent.setTimestamp(timestamp);
        awsLogEvent.setMessage(message);
        if (!queue.offer(awsLogEvent) && !queueFull) {
            queueFull = true;
        } else if (queueFull) {
            queueFull = false;
        }
    }

    private String createLogGroupAndLogStreamIfNeeded() {

        if (this.createLogGroupIfNeeded) {
            LOGGER.debug("Attempting to locate the log group [{}]", logGroupName);
            val describeLogGroupsResult =
                    awsLogsClient.describeLogGroups(new DescribeLogGroupsRequest().withLogGroupNamePrefix(logGroupName));
            var createLogGroup = true;
            if (describeLogGroupsResult != null && describeLogGroupsResult.getLogGroups() != null && !describeLogGroupsResult.getLogGroups().isEmpty()) {
                createLogGroup = describeLogGroupsResult.getLogGroups().stream().noneMatch(g -> g.getLogGroupName().equals(logGroupName));
            }
            if (createLogGroup) {
                LOGGER.debug("Creating log group [{}]", logGroupName);
                val createLogGroupRequest = new CreateLogGroupRequest(logGroupName);
                awsLogsClient.createLogGroup(createLogGroupRequest);
            }
        }

        var logSequenceToken = StringUtils.EMPTY;
        var createLogStream = true;
        LOGGER.debug("Attempting to locate the log stream [{}] for group [{}]", logStreamName, logGroupName);
        val describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName).withLogStreamNamePrefix(logStreamName);
        val describeLogStreamsResult = awsLogsClient.describeLogStreams(describeLogStreamsRequest);
        if (describeLogStreamsResult != null && describeLogStreamsResult.getLogStreams() != null && !describeLogStreamsResult.getLogStreams().isEmpty()) {
            for (val ls : describeLogStreamsResult.getLogStreams()) {
                if (logStreamName.equals(ls.getLogStreamName())) {
                    createLogStream = false;
                    logSequenceToken = ls.getUploadSequenceToken();
                    LOGGER.debug("Found log stream [{}] with sequence token [{}]", logStreamName, logSequenceToken);
                    break;
                }
            }
        }

        if (createLogStream) {
            if (!this.createLogStreamIfNeeded) {
                throw new RuntimeException("Log stream does not exist, yet `createIfNeeded` is false. This will not work");
            } else {
                LOGGER.debug("Creating log stream [{}] for group [{}]", logStreamName, logGroupName);
                val createLogStreamRequest = new CreateLogStreamRequest(logGroupName, logStreamName);
                awsLogsClient.createLogStream(createLogStreamRequest);
            }
        }
        return logSequenceToken;
    }


    @Override
    public void start() {
        super.start();
        this.deliveryThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    flush();
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (!shutdown && queue.size() < AWS_DRAIN_LIMIT) {
                    try {
                        synchronized (monitor) {
                            monitor.wait(flushPeriodMillis);
                        }
                    } catch (final InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            while (!queue.isEmpty()) {
                flush();
            }
        }, "CloudWatchAppenderDeliveryThread");
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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        if (!queue.isEmpty()) {
            flush();
        }
    }
}
