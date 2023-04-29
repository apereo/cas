package org.apereo.cas.logging;

import lombok.val;
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
import org.apache.logging.log4j.util.SortedArrayStringMap;

/**
 * This is {@link GoogleCloudAppender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Plugin(name = "GoogleCloudAppender", category = "Core", elementType = "appender", printObject = true)
public class GoogleCloudAppender extends AbstractAppender {
    private final Configuration config;

    private final AppenderRef appenderRef;

    public GoogleCloudAppender(final String name, final Configuration config, final AppenderRef appenderRef, final Filter filter) {
        super(name, filter, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        this.config = config;
        this.appenderRef = appenderRef;
    }

    /**
     * Build google cloud appender.
     *
     * @param name        the name
     * @param appenderRef the appender ref
     * @param filter      the filter
     * @param config      the config
     * @return the google cloud appender
     */
    @PluginFactory
    public static GoogleCloudAppender build(
        @PluginAttribute("name")
        final String name,
        @PluginElement("AppenderRef")
        final AppenderRef appenderRef,
        @PluginElement("Filter")
        final Filter filter,
        @PluginConfiguration
        final Configuration config) {
        return new GoogleCloudAppender(name, config, appenderRef, filter);
    }

    @Override
    public void append(final LogEvent logEvent) {
        val refName = this.appenderRef.getRef();
        val appender = this.config.getAppender(refName);
        val contextData = new SortedArrayStringMap(logEvent.getContextData());
        contextData.putValue("SourceLocation",
            logEvent.getSource() != null ? logEvent.getSource().toString() : "Unknown");
        contextData.putValue("SpanId", "Unknown");
        contextData.putValue("Trace", "Unknown");
        val newLogEvent = LoggingUtils.getLogEventBuilder(logEvent)
            .setContextData(contextData)
            .build();
        appender.append(newLogEvent);
    }
}
