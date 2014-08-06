/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

package org.slf4j.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CAS logger wrapper, which uses a substitute logger to route the logs.
 * This component only exists to intercept logging calls before they are
 * sent to the logging engine (log4j, etc) and serves to manipulate
 * logging messages if needed, such as removing sensitive ticket id from
 * the log message.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CasDelegatingLogger extends MarkerIgnoringBase implements Serializable {

    private static final long serialVersionUID = 6182834493563598289L;

    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(" + TicketGrantingTicket.PREFIX + "|"
            + TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
            + ")(-)*(\\w)*(-)*(\\w)*");
    
    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_ID_TAIL_LENGTH = 10;

    private final Logger delegate;

    /**
     * Instantiates a new Cas delegating logger.
     *
     * @param delegate the delegate
     */
    public CasDelegatingLogger(final Logger delegate) {
        this.delegate = delegate;
    }

    /**
     * Manipulate the log message. For now, removes ticket ids from the log.
     * @param msg log message
     * @return message to log
     */
    private String manipulateLogMessage(final String msg) {
        return removeTicketIdFromMessage(msg);
    }

    /**
     * Remove ticket id from the log message.
     *
     * @param msg the message
     * @return the modified message with tgt id removed
     */
    private String removeTicketIdFromMessage(final String msg) {
        final Matcher matcher = TICKET_ID_PATTERN.matcher(msg);
        if (matcher.find()) {
            final String match = matcher.group();
            final String newId = matcher.group(1) + "-"
                    + StringUtils.repeat("*", match.length() - VISIBLE_ID_TAIL_LENGTH)
                    + StringUtils.right(match, VISIBLE_ID_TAIL_LENGTH);

            return msg.replaceAll(match, newId);
        }
        return msg;
    }

    @Override
    public void trace(final String format, final Object arg) {
        delegate.trace(manipulateLogMessage(format), arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        delegate.trace(manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        delegate.trace(manipulateLogMessage(format), arguments);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        delegate.trace(manipulateLogMessage(msg), t);
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        delegate.trace(marker, manipulateLogMessage(msg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        delegate.trace(marker, manipulateLogMessage(format), arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.trace(marker, manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
        delegate.trace(marker, manipulateLogMessage(format), arguments);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        delegate.trace(marker, manipulateLogMessage(msg), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(final String msg) {
        delegate.debug(manipulateLogMessage(msg));
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        delegate.debug(manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        delegate.debug(manipulateLogMessage(format), arguments);
    }

    @Override
    public void debug(final String format, final Object arg) {
        delegate.debug(manipulateLogMessage(format), arg);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        delegate.debug(manipulateLogMessage(msg), t);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        delegate.debug(marker, manipulateLogMessage(msg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        delegate.debug(marker, manipulateLogMessage(format), arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.debug(marker, manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        delegate.debug(marker, manipulateLogMessage(format), arguments);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        delegate.debug(marker, manipulateLogMessage(msg), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(final String format, final Object arg) {
        delegate.info(manipulateLogMessage(format), arg);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        delegate.info(manipulateLogMessage(msg), t);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        delegate.info(manipulateLogMessage(format), arguments);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        delegate.info(manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        delegate.info(marker, manipulateLogMessage(msg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        delegate.info(marker, manipulateLogMessage(format), arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        delegate.info(marker, manipulateLogMessage(format), arguments);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.info(marker, manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        delegate.info(marker, manipulateLogMessage(msg), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(final String msg) {
        delegate.warn(manipulateLogMessage(msg));
    }

    @Override
    public void warn(final String format, final Object arg) {
        delegate.warn(manipulateLogMessage(format), arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        delegate.warn(manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        delegate.warn(manipulateLogMessage(format), arguments);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        delegate.warn(manipulateLogMessage(msg), t);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        delegate.warn(marker, manipulateLogMessage(msg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        delegate.warn(marker, manipulateLogMessage(format), arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.warn(marker, manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        delegate.warn(marker, manipulateLogMessage(format), arguments);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        delegate.warn(marker, manipulateLogMessage(msg), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(final String msg) {
        delegate.error(manipulateLogMessage(msg));
    }

    @Override
    public void error(final String format, final Object arg) {
        delegate.error(manipulateLogMessage(format), arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        delegate.error(manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        delegate.error(manipulateLogMessage(format), arguments);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        delegate.error(manipulateLogMessage(msg), t);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        delegate.error(marker, manipulateLogMessage(msg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        delegate.error(marker, manipulateLogMessage(format), arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.error(marker, manipulateLogMessage(format), arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        delegate.error(marker, manipulateLogMessage(format), arguments);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        delegate.error(marker, manipulateLogMessage(msg), t);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(final String msg) {
        delegate.trace(manipulateLogMessage(msg));
    }

    @Override
    public void info(final String msg) {
        delegate.info(manipulateLogMessage(msg));
    }
}
