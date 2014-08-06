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
        delegate.trace(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        delegate.trace(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        delegate.trace(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        delegate.trace(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        delegate.trace(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        delegate.trace(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.trace(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
        delegate.trace(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        delegate.trace(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(final String msg) {
        delegate.debug(removeTicketIdFromMessage(msg));
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        delegate.debug(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        delegate.debug(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void debug(final String format, final Object arg) {
        delegate.debug(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        delegate.debug(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        delegate.debug(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        delegate.debug(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.debug(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        delegate.debug(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        delegate.debug(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(final String format, final Object arg) {
        delegate.info(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        delegate.info(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        delegate.info(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        delegate.info(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        delegate.info(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        delegate.info(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        delegate.info(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.info(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        delegate.info(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(final String msg) {
        delegate.warn(removeTicketIdFromMessage(msg));
    }

    @Override
    public void warn(final String format, final Object arg) {
        delegate.warn(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        delegate.warn(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        delegate.warn(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        delegate.warn(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        delegate.warn(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        delegate.warn(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.warn(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        delegate.warn(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        delegate.warn(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(final String msg) {
        delegate.error(removeTicketIdFromMessage(msg));
    }

    @Override
    public void error(final String format, final Object arg) {
        delegate.error(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        delegate.error(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        delegate.error(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        delegate.error(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        delegate.error(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        delegate.error(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.error(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        delegate.error(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        delegate.error(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(final String msg) {
        delegate.trace(removeTicketIdFromMessage(msg));
    }

    @Override
    public void info(final String msg) {
        delegate.info(removeTicketIdFromMessage(msg));
    }
}
