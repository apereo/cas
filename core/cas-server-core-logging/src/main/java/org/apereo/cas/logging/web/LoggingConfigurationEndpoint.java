package org.apereo.cas.logging.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Endpoint(id = "loggingConfig", enableByDefault = false)
@Getter
public class LoggingConfigurationEndpoint extends BaseCasActuatorEndpoint implements InitializingBean {

    private static final String LOGGER_NAME_ROOT = "root";

    private static final String FILE_PARAM = "file";

    private static final String FILE_PATTERN_PARAM = "filePattern";

    private final Environment environment;

    private final ResourceLoader resourceLoader;

    private LoggerContext loggerContext;

    private Resource logConfigurationFile;

    public LoggingConfigurationEndpoint(final CasConfigurationProperties casProperties,
                                        final ResourceLoader resourceLoader,
                                        final Environment environment) {
        super(casProperties);
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    private static ILoggerFactory getCasLoggerFactoryInstance() {
        return LoggerFactory.getILoggerFactory();
    }

    @SneakyThrows
    private static Optional<Pair<Resource, LoggerContext>> buildLoggerContext(final Environment environment, final ResourceLoader
        resourceLoader) {
        val logFile = environment.getProperty("logging.config", "classpath:/log4j2.xml");
        LOGGER.info("Located logging configuration reference in the environment as [{}]", logFile);

        if (ResourceUtils.doesResourceExist(logFile, resourceLoader)) {
            val logConfigurationFile = resourceLoader.getResource(logFile);
            LOGGER.trace("Loaded logging configuration resource [{}]. Initializing logger context...", logConfigurationFile);
            val loggerContext = Configurator.initialize("CAS", null, logConfigurationFile.getURI());
            LOGGER.trace("Installing log configuration listener to detect changes and update");
            loggerContext.getConfiguration().addListener(reconfigurable -> loggerContext.updateLoggers(reconfigurable.reconfigure()));
            return Optional.of(Pair.of(logConfigurationFile, loggerContext));
        }
        LOGGER.warn("Logging configuration cannot be found in the environment settings");
        return Optional.empty();
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    /**
     * Init. Attempts to locate the logging configuration to insert listeners.
     * The log configuration location is pulled directly from the environment
     * given there is not an explicit property mapping for it provided by Boot, etc.
     */
    public void initialize() {
        val pair = buildLoggerContext(environment, resourceLoader);
        pair.ifPresent(it -> {
            this.logConfigurationFile = it.getKey();
            this.loggerContext = it.getValue();
        });
    }

    /**
     * Configuration map.
     *
     * @return the map
     */
    @ReadOperation
    public Map<String, Object> configuration() {
        val configuredLoggers = new HashSet<Object>();
        getLoggerConfigurations().forEach(config -> {
            val loggerMap = new HashMap<String, Object>();
            loggerMap.put("name", StringUtils.defaultIfBlank(config.getName(), LOGGER_NAME_ROOT));
            loggerMap.put("state", config.getState());
            if (config.getPropertyList() != null) {
                loggerMap.put("properties", config.getPropertyList());
            }
            loggerMap.put("additive", config.isAdditive());
            loggerMap.put("level", config.getLevel().name());
            val appenders = new HashSet<Object>();
            config.getAppenders().keySet().stream().map(key -> config.getAppenders().get(key)).forEach(appender -> {
                val builder = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
                builder.append("name", appender.getName());
                builder.append("state", appender.getState());
                builder.append("layoutFormat", appender.getLayout().getContentFormat());
                builder.append("layoutContentType", appender.getLayout().getContentType());
                if (appender instanceof FileAppender) {
                    builder.append(FILE_PARAM, ((FileAppender) appender).getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof RandomAccessFileAppender) {
                    builder.append(FILE_PARAM, ((RandomAccessFileAppender) appender).getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof RollingFileAppender) {
                    builder.append(FILE_PARAM, ((RollingFileAppender) appender).getFileName());
                    builder.append(FILE_PATTERN_PARAM, ((RollingFileAppender) appender).getFilePattern());
                }
                if (appender instanceof MemoryMappedFileAppender) {
                    builder.append(FILE_PARAM, ((MemoryMappedFileAppender) appender).getFileName());
                    builder.append(FILE_PATTERN_PARAM, "(none)");
                }
                if (appender instanceof RollingRandomAccessFileAppender) {
                    builder.append(FILE_PARAM, ((RollingRandomAccessFileAppender) appender).getFileName());
                    builder.append(FILE_PATTERN_PARAM, ((RollingRandomAccessFileAppender) appender).getFilePattern());
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

    private Map<String, Logger> getActiveLoggersInFactory() {
        val factory = (Log4jLoggerFactory) getCasLoggerFactoryInstance();
        if (factory != null) {
            return factory.getLoggersInContext(this.loggerContext);
        }
        return new HashMap<>(0);
    }

    /**
     * Gets logger configurations.
     *
     * @return the logger configurations
     */
    private Set<LoggerConfig> getLoggerConfigurations() {
        val configuration = this.loggerContext.getConfiguration();
        return new HashSet<>(configuration.getLoggers().values());
    }

    /**
     * Looks up the logger in the logger factory,
     * and attempts to find the real logger instance
     * based on the underlying logging framework
     * and retrieve the logger object. Then, updates the level.
     * This functionality at this point is heavily dependant
     * on the log4j API.
     *
     * @param loggerName  the logger name
     * @param loggerLevel the logger level
     * @param additive    the additive nature of the logger
     */
    @WriteOperation
    public void updateLoggerLevel(@Selector final String loggerName,
                                  final String loggerLevel,
                                  final boolean additive) {


        val loggerConfigs = getLoggerConfigurations();
        loggerConfigs.stream()
            .filter(cfg -> cfg.getName().equals(loggerName))
            .forEachOrdered(cfg -> {
                cfg.setLevel(Level.getLevel(loggerLevel));
                cfg.setAdditive(additive);
            });
        this.loggerContext.updateLoggers();
    }
}
