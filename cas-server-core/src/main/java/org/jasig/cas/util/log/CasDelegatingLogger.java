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

package org.jasig.cas.util.log;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.SubstituteLogger;

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
public final class CasDelegatingLogger extends SubstituteLogger {

    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(" + TicketGrantingTicket.PREFIX + "|"
            + TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
            + ")(-)*(\\w)*(-)*(\\w)*");
    
    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_ID_TAIL_LENGTH = 10;

    /**
     * Instantiates a new Cas delegating logger.
     *
     * @param name the name
     * @param delegate the delegate
     */
    public CasDelegatingLogger(final String name, final Logger delegate) {
        super(name);
        setDelegate(delegate);
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
        super.trace(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        super.trace(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        super.trace(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        super.trace(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        super.trace(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        super.trace(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        super.trace(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
        super.trace(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        super.trace(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void debug(final String msg) {
        super.debug(removeTicketIdFromMessage(msg));
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        super.debug(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        super.debug(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void debug(final String format, final Object arg) {
        super.debug(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        super.debug(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        super.debug(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        super.debug(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        super.debug(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        super.debug(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        super.debug(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void info(final String format, final Object arg) {
        super.info(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        super.info(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        super.info(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        super.info(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        super.info(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        super.info(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        super.info(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        super.info(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        super.info(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void warn(final String msg) {
        super.warn(removeTicketIdFromMessage(msg));
    }

    @Override
    public void warn(final String format, final Object arg) {
        super.warn(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        super.warn(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        super.warn(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        super.warn(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        super.warn(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        super.warn(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        super.warn(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        super.warn(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        super.warn(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void error(final String msg) {
        super.error(removeTicketIdFromMessage(msg));
    }

    @Override
    public void error(final String format, final Object arg) {
        super.error(removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        super.error(removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        super.error(removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        super.error(removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        super.error(marker, removeTicketIdFromMessage(msg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        super.error(marker, removeTicketIdFromMessage(format), arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        super.error(marker, removeTicketIdFromMessage(format), arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        super.error(marker, removeTicketIdFromMessage(format), arguments);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        super.error(marker, removeTicketIdFromMessage(msg), t);
    }

    @Override
    public void trace(final String msg) {
        super.trace(removeTicketIdFromMessage(msg));
    }

    @Override
    public void info(final String msg) {
        super.info(removeTicketIdFromMessage(msg));
    }
}
