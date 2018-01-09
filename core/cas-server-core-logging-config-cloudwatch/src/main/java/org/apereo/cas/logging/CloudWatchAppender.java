package org.apereo.cas.logging;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.CreateLogGroupRequest;
import com.amazonaws.services.logs.model.CreateLogStreamRequest;
import com.amazonaws.services.logs.model.DataAlreadyAcceptedException;
import com.amazonaws.services.logs.model.DescribeLogGroupsRequest;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.InvalidSequenceTokenException;
import com.amazonaws.services.logs.model.LogStream;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is {@link CloudWatchAppender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Plugin(name = "CloudWatchAppender", category = "Core", elementType = "appender", printObject = true)
public class CloudWatchAppender extends AbstractAppender {
    private static final long serialVersionUID = 1044758913028847477L;

    private static final int AWS_DRAIN_LIMIT = 256;
    private static final int AWS_LOG_STREAM_MAX_QUEUE_DEPTH = 10000;
    private static final int SHUTDOWN_TIMEOUT_MILLIS = 10000;
    private static final int AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS = 5;

    private final BlockingQueue<InputLogEvent> queue = new LinkedBlockingQueue<>(AWS_LOG_STREAM_MAX_QUEUE_DEPTH);
    private volatile boolean shutdown;
    private int flushPeriodMillis;
    private Thread deliveryThread;
    private final Object monitor = new Object();

    /**
     * Every PutLogEvents request must include the sequenceToken obtained from the response of the previous request.
     */
    private String sequenceTokenCache;
    private long lastReportedTimestamp = -1;

    private String logGroupName;
    private String logStreamName;
    private AWSLogs awsLogsClient;
    private volatile boolean queueFull;

    public CloudWatchAppender(final String name,
                              final String awsLogGroupName,
                              final String awsLogStreamName,
                              final String awsLogStreamFlushPeriodInSeconds,
                              final String credentialAccessKey,
                              final String credentialSecretKey,
                              final String awsLogRegionName,
                              final Layout<Serializable> layout) {
        super(name, null, layout == null ? PatternLayout.createDefaultLayout() : layout, false);
        try {
            int flushPeriod = AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS;
            if (awsLogStreamFlushPeriodInSeconds != null) {
                flushPeriod = Integer.parseInt(awsLogStreamFlushPeriodInSeconds);
            }
            flushPeriodMillis = flushPeriod * 1_000;

            LOGGER.debug("Connecting to AWS CloudWatch...");
            final AWSLogsClientBuilder builder = AWSLogsClient.builder();

            if (StringUtils.isNotBlank(credentialAccessKey) && StringUtils.isNotBlank(credentialSecretKey)) {
                LOGGER.debug("Loading AWS credentials directly from the logging configuration");
                final BasicAWSCredentials credentials = new BasicAWSCredentials(credentialAccessKey, credentialSecretKey);
                builder.setCredentials(new AWSStaticCredentialsProvider(credentials));
            } else {
                LOGGER.debug("Loading AWS credentials directly from the EC2 instance profile metadata");
                builder.setCredentials(new InstanceProfileCredentialsProvider(false));
            }
            
            builder.setRegion(awsLogRegionName);
            this.awsLogsClient = builder.build();
            this.logGroupName = awsLogGroupName;
            this.logStreamName = awsLogStreamName;
            this.sequenceTokenCache = createLogGroupAndLogStreamIfNeeded();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void flush() {
        int drained;
        final List<InputLogEvent> logEvents = new ArrayList<>(AWS_DRAIN_LIMIT);
        do {
            drained = queue.drainTo(logEvents, AWS_DRAIN_LIMIT);
            if (logEvents.isEmpty()) {
                break;
            }
            logEvents.sort(Comparator.comparing(InputLogEvent::getTimestamp));
            if (lastReportedTimestamp > 0) {
                for (final InputLogEvent event : logEvents) {
                    if (event.getTimestamp() < lastReportedTimestamp) {
                        event.setTimestamp(lastReportedTimestamp);
                    }
                }
            }

            lastReportedTimestamp = logEvents.get(logEvents.size() - 1).getTimestamp();
            final PutLogEventsRequest putLogEventsRequest = new PutLogEventsRequest(logGroupName, logStreamName, logEvents);
            putLogEventsRequest.setSequenceToken(sequenceTokenCache);
            try {
                final PutLogEventsResult putLogEventsResult = awsLogsClient.putLogEvents(putLogEventsRequest);
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
        final LogEvent event = LoggingUtils.prepareLogEvent(logEvent);
        final InputLogEvent awsLogEvent = new InputLogEvent();
        final long timestamp = event.getTimeMillis();
        final String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        awsLogEvent.setTimestamp(timestamp);
        awsLogEvent.setMessage(message);
        if (!queue.offer(awsLogEvent) && !queueFull) {
            queueFull = true;
        } else if (queueFull) {
            queueFull = false;
        }
    }

    private String createLogGroupAndLogStreamIfNeeded() {
        LOGGER.debug("Attempting to locate the log group [{}]", logGroupName);
        final DescribeLogGroupsResult describeLogGroupsResult =
                awsLogsClient.describeLogGroups(new DescribeLogGroupsRequest().withLogGroupNamePrefix(logGroupName));
        boolean createLogGroup = true;
        if (describeLogGroupsResult != null && describeLogGroupsResult.getLogGroups() != null && !describeLogGroupsResult.getLogGroups().isEmpty()) {
            createLogGroup = !describeLogGroupsResult.getLogGroups().stream().anyMatch(g -> g.getLogGroupName().equals(logGroupName));
        }
        if (createLogGroup) {
            LOGGER.debug("Creating log group [{}]", logGroupName);
            final CreateLogGroupRequest createLogGroupRequest = new CreateLogGroupRequest(logGroupName);
            awsLogsClient.createLogGroup(createLogGroupRequest);
        }
        String logSequenceToken = null;
        boolean createLogStream = true;
        LOGGER.debug("Attempting to locate the log stream [{}] for group [{}]", logStreamName, logGroupName);
        final DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName).withLogStreamNamePrefix(logStreamName);
        final DescribeLogStreamsResult describeLogStreamsResult = awsLogsClient.describeLogStreams(describeLogStreamsRequest);
        if (describeLogStreamsResult != null && describeLogStreamsResult.getLogStreams() != null && !describeLogStreamsResult.getLogStreams().isEmpty()) {
            for (final LogStream ls : describeLogStreamsResult.getLogStreams()) {
                if (logStreamName.equals(ls.getLogStreamName())) {
                    createLogStream = false;
                    logSequenceToken = ls.getUploadSequenceToken();
                    LOGGER.debug("Found log stream [{}] with sequence token [{}]", logStreamName, logSequenceToken);
                    break;
                }
            }
        }

        if (createLogStream) {
            LOGGER.debug("Creating log stream [{}] for group [{}]", logStreamName, logGroupName);
            final CreateLogStreamRequest createLogStreamRequest = new CreateLogStreamRequest(logGroupName, logStreamName);
            awsLogsClient.createLogStream(createLogStreamRequest);
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
                monitor.notify();
            }
            try {
                deliveryThread.join(SHUTDOWN_TIMEOUT_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (queue.size() > 0) {
            flush();
        }
    }

    /**
     * Create appender cloud watch appender.
     *
     * @param name                             the name
     * @param awsLogStreamName                 the aws log stream name
     * @param awsLogGroupName                  the aws log group name
     * @param awsLogStreamFlushPeriodInSeconds the aws log stream flush period in seconds
     * @param credentialAccessKey              the credential access key
     * @param credentialSecretKey              the credential secret key
     * @param awsLogRegionName                 the aws log region name
     * @param layout                           the layout
     * @return the cloud watch appender
     */
    @PluginFactory
    public static CloudWatchAppender createAppender(@PluginAttribute("name") final String name,
                                                    @PluginAttribute("awsLogStreamName") final String awsLogStreamName,
                                                    @PluginAttribute("awsLogGroupName") final String awsLogGroupName,
                                                    @PluginAttribute("awsLogStreamFlushPeriodInSeconds") final String awsLogStreamFlushPeriodInSeconds,
                                                    @PluginAttribute("credentialAccessKey") final String credentialAccessKey,
                                                    @PluginAttribute("credentialSecretKey") final String credentialSecretKey,
                                                    @PluginAttribute("awsLogRegionName") final String awsLogRegionName,
                                                    @PluginElement("Layout") final Layout<Serializable> layout) {
        return new CloudWatchAppender(
                name,
                awsLogGroupName,
                awsLogStreamName,
                awsLogStreamFlushPeriodInSeconds,
                StringUtils.defaultIfBlank(credentialAccessKey, System.getProperty("AWS_ACCESS_KEY")),
                StringUtils.defaultIfBlank(credentialSecretKey, System.getProperty("AWS_SECRET_KEY")),
                StringUtils.defaultIfBlank(awsLogRegionName, System.getProperty("AWS_REGION_NAME")),
                layout);
    }
}
