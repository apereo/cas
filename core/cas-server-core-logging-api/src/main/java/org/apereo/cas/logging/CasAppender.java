package org.apereo.cas.logging;

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
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * This is {@link CasAppender}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Plugin(name = "CasAppender", category = "Core", elementType = "appender", printObject = true)
public class CasAppender extends AbstractAppender {
    private final Configuration config;

    private final AppenderRef appenderRef;

    /**
     * Instantiates a new CAS appender.
     *
     * @param name        the name
     * @param config      the config
     * @param appenderRef the appender ref
     */
    public CasAppender(final String name, final Configuration config, final AppenderRef appenderRef) {
        super(name, null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        this.config = config;
        this.appenderRef = appenderRef;
    }

    /**
     * Instantiates a new CAS appender.
     *
     * @param name                the name
     * @param config              the config
     * @param appenderRef         the appender ref
     * @param filter              a filter (e.g. ExceptionOnlyFilter)
     */
    public CasAppender(final String name, final Configuration config, final AppenderRef appenderRef, final Filter filter) {
        super(name, filter, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        this.config = config;
        this.appenderRef = appenderRef;
    }

    /**
     * Create appender cas appender.
     *
     * @param name                the name
     * @param appenderRef         the appender ref
     * @param filter              the optional Filter
     * @param config              the config
     * @return the cas appender
     */
    @PluginFactory
    public static CasAppender build(@PluginAttribute("name") final String name,
                                    @PluginElement("AppenderRef") final AppenderRef appenderRef,
                                    @PluginElement("Filter") final Filter filter,
                                    @PluginConfiguration final Configuration config) {
        return new CasAppender(name, config, appenderRef, filter);
    }

    @Override
    public void append(final LogEvent logEvent) {
        val refName = this.appenderRef.getRef();
        if (StringUtils.isNotBlank(refName)) {
            val appender = this.config.getAppender(refName);
            if (appender != null) {
                val newLogEvent = LoggingUtils.prepareLogEvent(logEvent);
                appender.append(newLogEvent);
            } else {
                LOGGER.warn("No log appender could be found for [{}]", refName);
            }
        } else {
            LOGGER.warn("No log appender reference for [{}] could be located in logging configuration.", refName);
        }
    }
}
