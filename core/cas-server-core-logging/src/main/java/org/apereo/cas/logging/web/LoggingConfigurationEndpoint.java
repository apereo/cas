package org.apereo.cas.logging.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.CasAppender;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Endpoint(id = "loggingConfig", defaultAccess = Access.NONE)
@Getter
public class LoggingConfigurationEndpoint extends BaseCasRestActuatorEndpoint {

    private static final String LOGGER_NAME_ROOT = "root";

    private static final String FILE_PARAM = "file";

    private static final String FILE_PATTERN_PARAM = "filePattern";

    private LoggerContext loggerContext;

    private Resource logConfigurationFile;

    public LoggingConfigurationEndpoint(final CasConfigurationProperties casProperties,
                                        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    /**
     * Configuration map.
     *
     * @return the map
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get logging configuration report")
    public Map<String, Object> configuration() {
        initializeIfNecessary();

        val configuredLoggers = new HashSet<>();
        getLoggerConfigurations().forEach(config -> {
            val loggerMap = new HashMap<String, Object>();
            loggerMap.put("name", StringUtils.defaultIfBlank(config.getName(), LOGGER_NAME_ROOT));
            loggerMap.put("state", config.getState());
            if (config.getPropertyList() != null) {
                loggerMap.put("properties", config.getPropertyList());
            }
            loggerMap.put("additive", config.isAdditive());
            loggerMap.put("level", config.getLevel().name());
            val appenders = new HashSet<>();
            config.getAppenders().keySet().stream().map(key -> config.getAppenders().get(key)).forEach(appender -> {
                val builder = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
                builder.append("name", appender.getName());
                builder.append("state", appender.getState());
                builder.append("layoutFormat", appender.getLayout().getContentFormat());
                builder.append("layoutContentType", appender.getLayout().getContentType());
                if (appender instanceof final FileAppender app) {
                    builder.append(FILE_PARAM, app.getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof final RandomAccessFileAppender app) {
                    builder.append(FILE_PARAM, app.getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof final RollingFileAppender app) {
                    builder.append(FILE_PARAM, app.getFileName());
                    builder.append(FILE_PATTERN_PARAM, app.getFilePattern());
                }
                if (appender instanceof final MemoryMappedFileAppender app) {
                    builder.append(FILE_PARAM, app.getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof final RollingRandomAccessFileAppender app) {
                    builder.append(FILE_PARAM, app.getFileName());
                    builder.append(FILE_PATTERN_PARAM, app.getFilePattern());
                }
                appenders.add(builder.build());
            });
            loggerMap.put("appenders", appenders);
            configuredLoggers.add(loggerMap);
        });
        val responseMap = new HashMap<String, Object>();
        responseMap.put("loggers", configuredLoggers);

        val loggers = getActiveLoggersInFactory();
        responseMap.put("activeLoggers", loggers.values());

        return responseMap;
    }

    /**
     * Gets log entries.
     *
     * @param count the count
     * @param level the level
     * @return the log entries
     */
    @GetMapping(path = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch the last X number of log entries from all CAS appenders configured to capture logs",
        parameters = {
            @Parameter(name = "count", in = ParameterIn.QUERY, description = "The number of log entries to fetch", required = false),
            @Parameter(name = "level", in = ParameterIn.QUERY, description = "The log level to filter statements", required = false)
        })
    public List getLogEntries(@RequestParam(name = "count", required = false, defaultValue = "50") final int count,
                              @RequestParam(name = "level", required = false) final String level,
                              @RequestParam(name = "name", required = false) final String name) {
        initializeIfNecessary();
        val configuration = loggerContext.getConfiguration();
        return configuration.getAppenders()
            .values()
            .stream()
            .filter(CasAppender.class::isInstance)
            .map(CasAppender.class::cast)
            .filter(appender -> StringUtils.isBlank(name) || appender.getName().equalsIgnoreCase(name))
            .map(appender -> {
                val currentEvents = appender.getLogEvents()
                    .stream()
                    .filter(logEvent -> StringUtils.isBlank(level) || logEvent.getLevel().name().equalsIgnoreCase(level))
                    .map(logEvent -> {
                        val timestamp = DateTimeUtils.zonedDateTimeOf(logEvent.getInstant().getEpochMillisecond());
                        return new LogEvent(logEvent.getMessage().getFormattedMessage(), timestamp, logEvent.getLevel().name());
                    })
                    .toList();
                val start = Math.max(0, currentEvents.size() - count);
                return currentEvents.subList(start, currentEvents.size());
            })
            .filter(events -> !events.isEmpty())
            .flatMap(List::stream)
            .toList();
    }
    
    private Map<String, Logger> getActiveLoggersInFactory() {
        val factory = (Log4jLoggerFactory) getCasLoggerFactoryInstance();
        if (factory != null) {
            return factory.getLoggersInContext(this.loggerContext);
        }
        return new HashMap<>();
    }

    private Set<LoggerConfig> getLoggerConfigurations() {
        val configuration = this.loggerContext.getConfiguration();
        return new HashSet<>(configuration.getLoggers().values());
    }

    private static ILoggerFactory getCasLoggerFactoryInstance() {
        return LoggerFactory.getILoggerFactory();
    }

    private Optional<Pair<Resource, LoggerContext>> buildLoggerContext() {
        val logFile = applicationContext.getEnvironment().getProperty("logging.config", "classpath:/log4j2.xml");
        LOGGER.info("Located logging configuration reference in the environment as [{}]", logFile);
        if (ResourceUtils.doesResourceExist(logFile, applicationContext)) {
            val configFile = applicationContext.getResource(logFile);
            LOGGER.trace("Loaded logging configuration resource [{}]. Initializing logger context...", configFile);
            val context = FunctionUtils.doUnchecked(() -> Configurator.initialize("CAS", null, configFile.getURI()));
            LOGGER.trace("Installing log configuration listener to detect changes and update");
            context.getConfiguration().addListener(reconfigurable -> context.updateLoggers(reconfigurable.reconfigure()));
            return Optional.of(Pair.of(configFile, context));
        }
        LOGGER.warn("Logging configuration cannot be found in the environment settings");
        return Optional.empty();
    }

    /**
     * Init. Attempts to locate the logging configuration to insert listeners.
     * The log configuration location is pulled directly from the environment
     * given there is not an explicit property mapping for it provided by Boot, etc.
     */
    private void initializeIfNecessary() {
        if (loggerContext == null) {
            val pair = buildLoggerContext();
            pair.ifPresent(it -> {
                this.logConfigurationFile = it.getKey();
                this.loggerContext = it.getValue();
            });
        }
    }

    public record LogEvent(String message, ZonedDateTime timestamp, String level) {
    }
}
