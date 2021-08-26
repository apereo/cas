package org.apereo.cas.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Deny any messages without an exception (for stack trace). Neutral on messages with stack trace to allow other filters.
 * @author Hal Deadman
 * @since 6.3
 */
@Plugin(name = "ExceptionOnlyFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class ExceptionOnlyFilter extends AbstractFilter {

    protected ExceptionOnlyFilter() {
        super(Result.ACCEPT, Result.DENY);
    }

    @Override
    public Result filter(final LogEvent event) {
        if (event.getThrown() != null) {
            return getOnMatch();
        }
        return getOnMismatch();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        return t != null ? getOnMatch() : getOnMismatch();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        return t != null ? getOnMatch() : getOnMismatch();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object... params) {
        if (params != null && params.length > 0 && params[params.length - 1] instanceof Throwable) {
            return getOnMatch();
        }
        return getOnMismatch();
    }

    /**
     * New builder.
     *
     * @return the builder
     */
    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends AbstractFilterBuilder<ExceptionOnlyFilter.Builder> implements org.apache.logging.log4j.core.util.Builder<ExceptionOnlyFilter> {

        @Override
        public ExceptionOnlyFilter build() {
            return new ExceptionOnlyFilter();
        }
    }
}
