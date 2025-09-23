package org.apereo.cas.logging;

import lombok.Getter;
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
import java.util.ArrayDeque;
import java.util.Queue;

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

    @Getter
    private final Queue<LogEvent> logEvents;

    private final int maxLogEntries;

    public CasAppender(final String name, final Configuration config,
                       final AppenderRef appenderRef, final int maxEntries) {
        this(name, config, appenderRef, null, maxEntries);
    }

    public CasAppender(final String name, final Configuration config, final AppenderRef appenderRef,
                       final Filter filter, final int maxEntries) {
        super(name, filter, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        this.config = config;
        this.appenderRef = appenderRef;
        this.maxLogEntries = maxEntries;
        this.logEvents = maxEntries <= 0 ? new ArrayDeque<>() : new ArrayDeque<>(maxEntries);
    }

    /**
     * Build cas appender.
     *
     * @param name        the name
     * @param appenderRef the appender ref
     * @param filter      the filter
     * @param config      the config
     * @return the cas appender
     */
    @PluginFactory
    public static CasAppender build(@PluginAttribute("name") final String name,
                                    @PluginElement("AppenderRef") final AppenderRef appenderRef,
                                    @PluginElement("Filter") final Filter filter,
                                    @PluginConfiguration final Configuration config,
                                    @PluginAttribute("maxEntries") final int maxEntries) {
        return new CasAppender(name, config, appenderRef, filter, maxEntries);
    }

    @Override
    public void append(final LogEvent logEvent) {
        val refName = appenderRef.getRef();
        if (StringUtils.isNotBlank(refName)) {
            val appender = config.getAppender(refName);
            if (appender != null) {
                val newLogEvent = LoggingUtils.prepareLogEvent(logEvent);
                appender.append(newLogEvent);
                if (maxLogEntries > 0) {
                    if (logEvents.size() >= maxLogEntries) {
                        logEvents.poll();
                    }
                    logEvents.add(newLogEvent.toImmutable());
                }
            } else {
                LOGGER.warn("No log appender could be found for [{}]", refName);
            }
        } else {
            LOGGER.warn("No log appender reference for [{}] could be located in logging configuration.", refName);
        }
    }
}
