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

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0) {
        return super.filter(logger, level, marker, msg, p0);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1) {
        return super.filter(logger, level, marker, msg, p0, p1);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2) {
        return super.filter(logger, level, marker, msg, p0, p1, p2);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3, p4);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                         final Object p6) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                         final Object p6, final Object p7) {
        return filter(logger, level, marker, msg, new Object[] {p0, p1, p2, p3, p4, p5, p6, p7});
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                         final Object p6, final Object p7, final Object p8) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0,
                         final Object p1, final Object p2, final Object p3, final Object p4, final Object p5,
                         final Object p6, final Object p7, final Object p8, final Object p9) {
        return super.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override
    public String toString() {
        return "ExceptionOnlyFilter";
    }

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
