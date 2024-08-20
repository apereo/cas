package org.apereo.cas.logging;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This is {@link CloudWatchLogsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Endpoint(id = "cloudWatchLogs", enableByDefault = false)
@Slf4j
public class CloudWatchLogsEndpoint extends BaseCasRestActuatorEndpoint {
    private static final Pattern LOG_LEVEL_PATTERN = Pattern.compile(".*(\\[*(ERROR|DEBUG|INFO|WARN|TRACE)\\]*).*", Pattern.CASE_INSENSITIVE);

    private final ObjectProvider<CloudWatchLogsClient> awsLogsClient;

    public CloudWatchLogsEndpoint(final CasConfigurationProperties casProperties,
                                  final ConfigurableApplicationContext applicationContext,
                                  final ObjectProvider<CloudWatchLogsClient> awsLogsClient) {
        super(casProperties, applicationContext);
        this.awsLogsClient = awsLogsClient;
    }

    /**
     * Fetch log entries as a list.
     *
     * @param count the count
     * @return the list
     */
    @GetMapping(path = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch the last X number of log entries from AWS cloud watch",
        parameters = @Parameter(name = "count", in = ParameterIn.QUERY, description = "The number of log entries to fetch", required = false))
    @SuppressWarnings("FutureReturnValueIgnored")
    public List<LogEvent> fetchLogEntries(@RequestParam(name = "count", required = false, defaultValue = "50") final int count) {
        val cloudwatch = casProperties.getLogging().getCloudwatch();
        val logEventsRequest = GetLogEventsRequest
            .builder()
            .logGroupName(cloudwatch.getLogGroupName())
            .logStreamName(cloudwatch.getLogStreamName())
            .limit(count)
            .startFromHead(false)
            .unmask(true)
            .build();
        val logEventsResponse = awsLogsClient.getObject().getLogEvents(logEventsRequest);
        return logEventsResponse
            .events()
            .stream()
            .map(event -> {
                var message = event.message();
                val matcher = LOG_LEVEL_PATTERN.matcher(message);
                var logLevel = "INFO";
                if (matcher.find()) {
                    logLevel = matcher.group(2).toUpperCase(Locale.ENGLISH);
                    message = StringUtils.remove(message, matcher.group(1));
                }
                return new LogEvent(message, DateTimeUtils.zonedDateTimeOf(event.timestamp()), logLevel);
            })
            .toList();
    }

    public record LogEvent(String message, ZonedDateTime timestamp, String level) {
    }
}
