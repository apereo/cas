/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.web.report;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.CasDelegatingLogger;
import org.slf4j.impl.CasLoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controller to handle the logging dashboard requests.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component
@Controller("loggingConfigController")
public class LoggingConfigController {

    @Value("${log4j.config.location:classpath:log4j2.xml}")
    private Resource logConfigurationFile;

    /**
     * Gets configuration as JSON.
     * Depends on the log4j core API.
     *
     * @param request the request
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping(value="/getConfiguration", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getConfiguration(final HttpServletRequest request, final HttpServletResponse response)
                throws Exception {
        final Map<String, Object> responseMap = new HashMap();

        final CasLoggerFactory factory = (CasLoggerFactory) LoggerFactory.getILoggerFactory();
        final Set<Map.Entry<String, CasDelegatingLogger>> loggers = factory.getLoggers().entrySet();
        responseMap.put("activeLoggers", loggers);

        final LoggerContext loggerContext = Configurator.initialize("CAS", null,
                this.logConfigurationFile.getURI());
        final Configuration configuration = loggerContext.getConfiguration();
        final Collection<LoggerConfig> loggerConfigs = configuration.getLoggers().values();

        final Collection<Map<String, Object>> configuredLoggers = new HashSet<>();
        for(final LoggerConfig config : loggerConfigs) {
            final Map<String, Object> loggerMap = new HashMap();
            loggerMap.put("name", StringUtils.defaultIfBlank(config.getName(), "Root"));
            loggerMap.put("state", config.getState());
            if (config.getProperties() != null) {
                loggerMap.put("properties", config.getProperties());
            }
            loggerMap.put("additive", config.isAdditive());
            loggerMap.put("level", config.getLevel().name());
            loggerMap.put("appenders", config.getAppenders().keySet());

            configuredLoggers.add(loggerMap);
        }
        responseMap.put("loggers", configuredLoggers);
        return responseMap;
    }

    /**
     * Looks up the logger in the logger factory,
     * and attempts to find the real logger instance
     * based on the underlying logging framework
     * and retrieve the logger object. Then, updates the level.
     * This functionality at this point is heavily dependant
     * on the log4j API.
     *
     * @param loggerName the logger name
     * @param loggerLevel the logger level
     * @param additive the additive nature of the logger
     * @param request the request
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping(value="/updateLoggerLevel", method = RequestMethod.POST)
    @ResponseBody
    public void updateLoggerLevel(@RequestParam final String loggerName,
                                  @RequestParam final String loggerLevel,
                                  @RequestParam(defaultValue = "true") final boolean additive,
                                  final HttpServletRequest request,
                                  final HttpServletResponse response)
            throws Exception {
        final CasLoggerFactory factory = (CasLoggerFactory) LoggerFactory.getILoggerFactory();
        final CasDelegatingLogger casLogger = factory.getLoggers().get(loggerName);
        final LocationAwareLogger delegatedLogger = (LocationAwareLogger) casLogger.getDelegate();
        final Field field = ReflectionUtils.findField(delegatedLogger.getClass(), "logger");
        field.setAccessible(true);
        final org.apache.logging.log4j.core.Logger logger =
                (org.apache.logging.log4j.core.Logger) ReflectionUtils.getField(field, delegatedLogger);
        logger.setLevel(Level.getLevel(loggerLevel));
        logger.setAdditive(additive);
    }
}
