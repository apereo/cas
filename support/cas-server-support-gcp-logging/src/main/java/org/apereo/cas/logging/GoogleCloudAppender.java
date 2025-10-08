package org.apereo.cas.logging;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.text.MessageSanitizer;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.cloud.spring.logging.StackdriverTraceConstants;
import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.JsonLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link GoogleCloudAppender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Plugin(name = "GoogleCloudAppender", category = "Core", elementType = "appender", printObject = true)
public class GoogleCloudAppender extends AbstractAppender {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final int TRACE_ID_64_BIT_LENGTH = 16;
    
    private static final List<String> TRACE_ID_MDC_FIELDS = List.of(
        StackdriverTraceConstants.MDC_FIELD_TRACE_ID,
        "X-B3-TraceId", "traceparent", "X-Cloud-Trace-Context");

    private final Configuration configuration;

    private final AppenderRef appenderRef;

    /**
     * If no Project ID set, then attempts to resolve it with the default project ID provider.
     */
    private final String projectId;

    private final Boolean flattenMessage;
    private final Boolean requiresLocation;

    private final Map<String, String> labels = new LinkedHashMap<>();

    public GoogleCloudAppender(final String name, final Configuration config,
                               final AppenderRef appenderRef,
                               final Filter filter,
                               final String projectId,
                               final Boolean flattenMessage,
                               final Boolean requiresLocation,
                               final String givenLabels) {
        super(name, filter, JsonLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        this.configuration = config;
        this.appenderRef = appenderRef;
        this.projectId = FunctionUtils.doIfNull(projectId, () -> {
            val projectIdProvider = new DefaultGcpProjectIdProvider();
            return projectIdProvider.getProjectId();
        }, () -> projectId).get();
        this.flattenMessage = flattenMessage;
        this.requiresLocation = requiresLocation;

        org.springframework.util.StringUtils.commaDelimitedListToSet(givenLabels)
            .forEach(label -> {
                val keyValue = Splitter.on('=').splitToList(label);
                labels.put(keyValue.getFirst(), keyValue.getLast());
            });
    }

    /**
     * Build google cloud appender.
     *
     * @param name           the name
     * @param projectId      the project id
     * @param flattenMessage the flatten message
     * @param appenderRef    the appender ref
     * @param filter         the filter
     * @param config         the config
     * @return the google cloud appender
     */
    @PluginFactory
    public static GoogleCloudAppender build(
        @PluginAttribute("name") final String name,
        @PluginAttribute(value = "projectId", sensitive = true) final String projectId,
        @PluginAttribute(value = "labels", sensitive = false, defaultString = "application=cas") final String labels,
        @PluginAttribute(value = "flattenMessage", defaultBoolean = false, sensitive = false) final Boolean flattenMessage,
        @PluginAttribute(value = "requiresLocation", defaultBoolean = true, sensitive = false) final Boolean requiresLocation,
        @PluginElement("AppenderRef") final AppenderRef appenderRef,
        @PluginElement("Filter") final Filter filter,
        @PluginConfiguration final Configuration config) {
        return new GoogleCloudAppender(name, config, appenderRef, filter, projectId, flattenMessage, requiresLocation, labels);
    }

    @Override
    public void append(final LogEvent logEvent) {
        val contextData = new SortedArrayStringMap(logEvent.getContextData());
        collectSourceLocation(logEvent, contextData);
        collectLabels(contextData);
        collectInsertId(contextData);
        collectTraceId(contextData);
        collectHttpRequest(contextData);
        collectTimestamps(logEvent, contextData);
        appendLogEvent(logEvent, contextData);
    }

    protected void collectInsertId(final SortedArrayStringMap contextData) {
        contextData.putValue("insertId", String.valueOf(configuration.getNanoClock().nanoTime()));
    }

    protected void collectLabels(final SortedArrayStringMap contextData) {
        contextData.putValue("labels", labels);
        labels.forEach((key, value) -> contextData.putValue("label-%s".formatted(key), value));
    }

    protected void collectSourceLocation(final LogEvent logEvent, final SortedArrayStringMap contextData) {
        val source = logEvent.getSource();
        if (source != null) {
            val sourceLocationMap = Map.of(
                "class", source.getClassName(),
                "function", source.getMethodName(),
                "file", StringUtils.defaultIfBlank(source.getFileName(), "N/A"),
                "line", source.getLineNumber());
            contextData.putValue("sourceLocation", sourceLocationMap);
            sourceLocationMap.forEach((key, value) -> contextData.putValue("sourceLocation-%s".formatted(key), value));
        }
    }

    protected void collectHttpRequest(final StringMap contextData) {
        val requestUrl = StringUtils.defaultString(contextData.getValue("requestUrl"));
        if (StringUtils.isNotBlank(requestUrl)) {
            val httpRequest = Map.of(
                "requestMethod", StringUtils.defaultString(contextData.getValue("method")),
                "requestUrl", requestUrl,
                "protocol", StringUtils.defaultString(contextData.getValue("protocol")),
                "userAgent", StringUtils.defaultString(contextData.getValue(HttpHeaders.USER_AGENT)),
                "remoteIp", StringUtils.defaultString(contextData.getValue("remoteAddress")));
            contextData.putValue("httpRequest", httpRequest);
            httpRequest.forEach((key, value) -> contextData.putValue("httpRequest-%s".formatted(key), value));
        }
    }

    protected void appendLogEvent(final LogEvent logEvent, final StringMap contextData) {
        val appender = configuration.getAppender(appenderRef.getRef());
        appender.append(finalizeLogEvent(logEvent, contextData));
    }

    protected LogEvent finalizeLogEvent(final LogEvent logEvent, final StringMap contextData) {
        val messagePayload = new LinkedHashMap<>();
        val message = buildLogMessage(logEvent, messagePayload);
        return LoggingUtils.getLogEventBuilder(logEvent)
            .setContextData(contextData)
            .setMessage(message)
            .build();
    }

    protected void collectTimestamps(final LogEvent logEvent, final StringMap contextData) {
        contextData.putValue(
            StackdriverTraceConstants.TIMESTAMP_SECONDS_ATTRIBUTE,
            String.valueOf(TimeUnit.MILLISECONDS.toSeconds(logEvent.getTimeMillis())));
        contextData.putValue(
            StackdriverTraceConstants.TIMESTAMP_NANOS_ATTRIBUTE,
            String.valueOf(TimeUnit.MILLISECONDS.toNanos(logEvent.getTimeMillis())));
    }

    protected void collectTraceId(final StringMap contextData) {
        if (StringUtils.isNotBlank(this.projectId)) {
            TRACE_ID_MDC_FIELDS
                .stream()
                .map(MDC::get)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .ifPresent(traceId -> contextData.putValue(StackdriverTraceConstants.MDC_FIELD_TRACE_ID,
                    StackdriverTraceConstants.composeFullTraceName(this.projectId, formatTraceId(traceId))));
        }
    }

    protected Message buildLogMessage(final LogEvent logEvent,
                                      final Map<Object, Object> messagePayload) {
        val buildMessage = logEvent.getMarker() == null
            || !logEvent.getMarker().getName().equals(AsciiArtUtils.ASCII_ART_LOGGER_MARKER.getName());
        if (buildMessage) {
            val messageSanitizer = ApplicationContextProvider.getMessageSanitizer().orElseGet(MessageSanitizer::disabled);
            val formattedMessage = messageSanitizer.sanitize(logEvent.getMessage().getFormattedMessage());
            if (flattenMessage) {
                return new ObjectMessage(formattedMessage);
            }
            messagePayload.put("text", formattedMessage);
            if (logEvent.getMessage() instanceof final ObjectMessage objectMessage) {
                if (objectMessage.getParameter() instanceof final Map parameters) {
                    collectMessageParameters(messagePayload, messageSanitizer, parameters);
                } else {
                    try {
                        val parameters = MAPPER.convertValue(objectMessage.getParameter(), Map.class);
                        collectMessageParameters(messagePayload, messageSanitizer, parameters);
                    } catch (final Exception e) {
                        val parameters = Map.of("payload", objectMessage.getParameter().toString());
                        collectMessageParameters(messagePayload, messageSanitizer, parameters);
                    }
                }
            }
        }
        return new ObjectMessage(messagePayload);
    }

    private static void collectMessageParameters(final Map<Object, Object> messagePayload,
                                                 final MessageSanitizer messageSanitizer,
                                                 final Map parameters) {
        parameters.forEach((key, value) -> messagePayload.put(key, messageSanitizer.sanitize(value.toString())));
    }

    protected String formatTraceId(final String traceId) {
        if (traceId != null && traceId.length() == TRACE_ID_64_BIT_LENGTH) {
            return "0000000000000000" + traceId;
        }
        return traceId;
    }

    @Override
    public boolean requiresLocation() {
        return this.requiresLocation;
    }
}
