package org.apereo.cas.logging;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * This is {@link SplunkAppender}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Plugin(name = "SplunkAppender", category = "Core", elementType = "appender", printObject = true)
@Slf4j
public class SplunkAppender extends AbstractAppender {
    private static final long serialVersionUID = 1244758357628847477L;

    private final Configuration config;
    private final AppenderRef appenderRef;

    public SplunkAppender(final String name, @NonNull final Configuration config, @NonNull final AppenderRef appenderRef) {
        super(name, null, PatternLayout.createDefaultLayout());
        this.config = config;
        this.appenderRef = appenderRef;
    }

    @Override
    public void append(final LogEvent logEvent) {
        final LogEvent newLogEvent = LoggingUtils.prepareLogEvent(logEvent);
        final String refName = this.appenderRef.getRef();
        if (StringUtils.isNotBlank(refName)) {
            final Appender appender = this.config.getAppender(refName);
            if (appender != null) {
                appender.append(newLogEvent);
            } else {
                LOGGER.warn("No Splunk log appender could be found for [{}]", refName);
            }
        } else {
            LOGGER.warn("No Splunk log appender reference could be located in logging configuration.");
        }
    }

    /**
     * Create appender.
     *
     * @param name        the name
     * @param appenderRef the appender ref
     * @param config      the config
     * @return the appender
     */
    @PluginFactory
    public static SplunkAppender build(@PluginAttribute("name") final String name,
                                       @PluginElement("AppenderRef") final AppenderRef appenderRef,
                                       @PluginConfiguration final Configuration config) {
        return new SplunkAppender(name, config, appenderRef);
    }
}
