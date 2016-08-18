package org.apereo.cas.web.report;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller to handle the logging dashboard requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Controller("loggingConfigController")
@RequestMapping("/status/logging")
public class LoggingConfigController {

    private static final String VIEW_CONFIG = "monitoring/viewLoggingConfig";
    private static final String LOGGER_NAME_ROOT = "root";

    @Value("${logging.config:classpath:log4j2.xml}")
    private Resource logConfigurationFile;

    private LoggerContext loggerContext;

    /**
     * Init.
     *
     * @throws Exception the exception
     */
    @PostConstruct
    public void initialize() throws Exception {
        this.loggerContext = Configurator.initialize("CAS", null, this.logConfigurationFile.getURI());
        this.loggerContext.getConfiguration().addListener(reconfigurable -> loggerContext.updateLoggers(reconfigurable.reconfigure()));
    }

    /**
     * Gets default view.
     *
     * @return the default view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getDefaultView() throws Exception {
        final Map<String, Object> model = new HashMap<>();
        model.put("logConfigurationFile", logConfigurationFile.getURI());
        return new ModelAndView(VIEW_CONFIG, model);
    }

    /**
     * Gets active loggers.
     *
     * @param request  the request
     * @param response the response
     * @return the active loggers
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getActiveLoggers", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getActiveLoggers(final HttpServletRequest request,
                                                final HttpServletResponse response)
            throws Exception {
        final Map<String, Object> responseMap = new HashMap<>();
        final Set<Map.Entry<String, Logger>> loggers = getActiveLoggersInFactory();
        final Collection<Logger> activeLoggers = loggers.stream().map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
        responseMap.put("activeLoggers", activeLoggers);
        return responseMap;
    }


    /**
     * Gets configuration as JSON.
     * Depends on the log4j core API.
     *
     * @param request  the request
     * @param response the response
     * @return the configuration
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getConfiguration", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getConfiguration(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Collection<Map<String, Object>> configuredLoggers = new HashSet<>();
        for (final LoggerConfig config : getLoggerConfigurations()) {

            final Map<String, Object> loggerMap = new HashMap<>();
            loggerMap.put("name", StringUtils.defaultIfBlank(config.getName(), LOGGER_NAME_ROOT));
            loggerMap.put("state", config.getState());
            if (config.getProperties() != null) {
                loggerMap.put("properties", config.getProperties());
            }
            loggerMap.put("additive", config.isAdditive());
            loggerMap.put("level", config.getLevel().name());

            final Collection<String> appenders = new HashSet<>();
            for (final String key : config.getAppenders().keySet()) {
                final Appender appender = config.getAppenders().get(key);
                final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
                builder.append("name", appender.getName());
                builder.append("state", appender.getState());
                builder.append("layoutFormat", appender.getLayout().getContentFormat());
                builder.append("layoutContentType", appender.getLayout().getContentType());

                if (appender instanceof FileAppender) {
                    builder.append("file", ((FileAppender) appender).getFileName());
                    builder.append("filePattern", "(none)");
                }
                if (appender instanceof RandomAccessFileAppender) {
                    builder.append("file", ((RandomAccessFileAppender) appender).getFileName());
                    builder.append("filePattern", "(none)");
                }
                if (appender instanceof RollingFileAppender) {
                    builder.append("file", ((RollingFileAppender) appender).getFileName());
                    builder.append("filePattern", ((RollingFileAppender) appender).getFilePattern());
                }
                if (appender instanceof MemoryMappedFileAppender) {
                    builder.append("file", ((MemoryMappedFileAppender) appender).getFileName());
                    builder.append("filePattern", "(none)");
                }
                if (appender instanceof RollingRandomAccessFileAppender) {
                    builder.append("file", ((RollingRandomAccessFileAppender) appender).getFileName());
                    builder.append("filePattern", ((RollingRandomAccessFileAppender) appender).getFilePattern());
                }
                appenders.add(builder.build());
            }
            loggerMap.put("appenders", appenders);

            configuredLoggers.add(loggerMap);
        }
        final Map<String, Object> responseMap = new HashMap();
        responseMap.put("loggers", configuredLoggers);
        return responseMap;
    }

    private Set<Map.Entry<String, Logger>> getActiveLoggersInFactory() {
        final ILoggerFactory factory = getCasLoggerFactoryInstance();
        return new HashSet<>();
    }

    private ILoggerFactory getCasLoggerFactoryInstance() {
        return LoggerFactory.getILoggerFactory();
    }


    /**
     * Gets logger configurations.
     *
     * @return the logger configurations
     * @throws IOException the iO exception
     */
    private Set<LoggerConfig> getLoggerConfigurations() throws IOException {
        final Configuration configuration = this.loggerContext.getConfiguration();
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
     * @param request     the request
     * @param response    the response
     * @throws Exception the exception
     */
    @RequestMapping(value = "/updateLoggerLevel", method = RequestMethod.POST)
    @ResponseBody
    public void updateLoggerLevel(@RequestParam final String loggerName,
                                  @RequestParam final String loggerLevel,
                                  @RequestParam(defaultValue = "false") final boolean additive,
                                  final HttpServletRequest request,
                                  final HttpServletResponse response)
            throws Exception {

        final Collection<LoggerConfig> loggerConfigs = getLoggerConfigurations();
        loggerConfigs.stream().
                filter(cfg -> cfg.getName().equals(loggerName))
                .forEachOrdered(cfg -> {
                    cfg.setLevel(Level.getLevel(loggerLevel));
                    cfg.setAdditive(additive);
                });
        this.loggerContext.updateLoggers();
    }

    /**
     * Gets logs.
     *
     * @param request  the request
     * @param response the response
     * @return the log output
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getLogOutput")
    @ResponseBody
    public Map<String, Object> getLogOutput(final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        final Collection<String> outputFileNames = new HashSet<>();
        final Collection<LoggerConfig> loggerConfigs = getLoggerConfigurations();
        for (final LoggerConfig config : loggerConfigs) {
            for (final String key : config.getAppenders().keySet()) {
                final Appender appender = config.getAppenders().get(key);
                if (appender instanceof FileAppender) {
                    outputFileNames.add(((FileAppender) appender).getFileName());
                } else if (appender instanceof RandomAccessFileAppender) {
                    outputFileNames.add(((RandomAccessFileAppender) appender).getFileName());
                } else if (appender instanceof RollingFileAppender) {
                    outputFileNames.add(((RollingFileAppender) appender).getFileName());
                } else if (appender instanceof MemoryMappedFileAppender) {
                    outputFileNames.add(((MemoryMappedFileAppender) appender).getFileName());
                } else if (appender instanceof RollingRandomAccessFileAppender) {
                    outputFileNames.add(((RollingRandomAccessFileAppender) appender).getFileName());
                }
            }
        }

        final Map<String, Object> responseMap = new HashMap();
        for (final String file : outputFileNames) {
            responseMap.put(file, FileUtils.readFileToString(new File(file), "UTF-8"));
        }
        return responseMap;
    }
}
