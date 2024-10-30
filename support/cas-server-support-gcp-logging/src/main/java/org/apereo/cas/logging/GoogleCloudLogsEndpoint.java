package org.apereo.cas.logging;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.google.cloud.logging.Logging;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link GoogleCloudLogsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Endpoint(id = "gcpLogs", defaultAccess = Access.NONE)
@Slf4j
public class GoogleCloudLogsEndpoint extends BaseCasRestActuatorEndpoint {
    private final Logging loggingService;

    public GoogleCloudLogsEndpoint(final CasConfigurationProperties casProperties,
                                   final ConfigurableApplicationContext applicationContext,
                                   final Logging loggingService) {
        super(casProperties, applicationContext);
        this.loggingService = loggingService;
    }

    /**
     * Fetch log entries as a list.
     *
     * @param count the count
     * @param level the level
     * @return the list
     */
    @GetMapping(path = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch the last X number of log entries from GCP",
        parameters = {
            @Parameter(name = "count", in = ParameterIn.QUERY, description = "The number of log entries to fetch", required = false),
            @Parameter(name = "level", in = ParameterIn.QUERY, description = "The log level to filter statements", required = false)
        })
    public List<LogEvent> fetchLogEntries(
        @RequestParam(name = "count", required = false, defaultValue = "50") final int count,
        @RequestParam(name = "level", required = false, defaultValue = "INFO") final String level) {

        val filterBuilder = new StringBuilder("severity=" + level.toUpperCase(Locale.ENGLISH));
        val properties = casProperties.getLogging().getGcp();
        if (StringUtils.isNotBlank(properties.getLogName())) {
            filterBuilder.append(" AND logName=").append(properties.getLogName());
        }
        for (val label : properties.getLabels().entrySet()) {
            filterBuilder.append(" AND resource.labels.").append(label.getKey()).append('=').append(label.getValue());
        }
        val filter = filterBuilder.toString();
        LOGGER.trace("Using filter [{}]", filter);
        
        val logEntries = loggingService.listLogEntries(
            Logging.EntryListOption.filter(filter),
            Logging.EntryListOption.sortOrder(Logging.SortingField.TIMESTAMP, Logging.SortingOrder.DESCENDING),
            Logging.EntryListOption.pageSize(count)
        ).iterateAll().iterator();

        val logEvents = new ArrayList<LogEvent>();
        while (logEntries.hasNext() && logEvents.size() < count) {
            val logEntry = logEntries.next();
            val event = new LogEvent(logEntry.getPayload().getData().toString(),
                DateTimeUtils.zonedDateTimeOf(logEntry.getInstantTimestamp()),
                logEntry.getSeverity().name().toUpperCase(Locale.ENGLISH),
                logEntry.getLabels());
            logEvents.add(event);
        }
        return logEvents;
    }

    public record LogEvent(String message, ZonedDateTime timestamp, String level, Map<String, String> labels) {
    }
}
