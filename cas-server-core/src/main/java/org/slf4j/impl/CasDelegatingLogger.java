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
package org.slf4j.impl;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CAS logger wrapper, which uses a substitute logger to route the logs.
 * This component only exists to intercept logging calls before they are
 * sent to the logging engine (log4j, etc) and serves to manipulate
 * logging messages if needed, such as removing sensitive ticket id from
 * the log message.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class CasDelegatingLogger extends MarkerIgnoringBase implements Serializable {

    private static final long serialVersionUID = 6182834493563598289L;

    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(" + TicketGrantingTicket.PREFIX + "|"
            + TicketGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX + "|" + TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
            + ")(-)*(\\w)*(-)*(\\w)*");

    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_ID_TAIL_LENGTH = 10;

    private final Logger delegate;

    /**
     * Instantiates a new Cas delegating logger.
     * Used for serialization purposes only.
     */
    private CasDelegatingLogger() {
        this.delegate = null;
    }

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
        return removeTicketId(msg);
    }

    /**
     * Manipulate the log arguments. For now, removes ticket ids from the log.
     * @param args log args
     * @return sanitized arguments
     */
    private Object[] manipulateLogArguments(final Object... args) {
        final Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                out[i] = removeTicketId(args[i].toString());
            } else {
                out[i] = null;
            }
        }
        return out;
    }

    /**
     * Remove ticket id from the log message.
     *
     * @param msg the message
     * @return the modified message with tgt id removed
     */
    private String removeTicketId(final String msg) {
        String modifiedMessage = msg;

        if (StringUtils.isNotBlank(msg)) {
            final Matcher matcher = TICKET_ID_PATTERN.matcher(msg);
            while (matcher.find()) {
                final String match = matcher.group();
                final String newId = matcher.group(1) + '-'
                        + StringUtils.repeat("*", match.length() - VISIBLE_ID_TAIL_LENGTH)
                        + StringUtils.right(match, VISIBLE_ID_TAIL_LENGTH);

                modifiedMessage = modifiedMessage.replaceAll(match, newId);
            }
        }
        return modifiedMessage;
    }

    /**
     * Gets exception to log.
     *
     * @param msg the error msg
     * @param t the exception to log. May be null if
     *          the underlying call did not specify an inner exception.
     * @return the exception message to log
     */
    private String getExceptionToLog(final String msg, final Throwable t) {
        final StringWriter sW = new StringWriter();
        final PrintWriter w = new PrintWriter(sW);
        w.println(manipulateLogMessage(msg));
        if (t != null) {
            t.printStackTrace(w);
        }

        final String log = sW.getBuffer().toString();
        return manipulateLogMessage(log);
    }

    /*
    * TRACE level logging
    */
    @Override
    public void trace(final String format, final Object arg) {
        delegate.trace(manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        delegate.trace(manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        delegate.trace(manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        delegate.trace(getExceptionToLog(msg, t));
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        delegate.trace(marker, manipulateLogMessage(msg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        delegate.trace(marker, manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.trace(marker, manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
        delegate.trace(marker, manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        delegate.trace(marker, getExceptionToLog(msg, t));
    }

    @Override
    public void trace(final String msg) {
        delegate.trace(manipulateLogMessage(msg));
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /*
    * DEBUG level logging
    */

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(final String format, final Object arg) {
        delegate.debug(manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        delegate.debug(manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        delegate.debug(manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        delegate.debug(getExceptionToLog(msg, t));
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        delegate.debug(marker, manipulateLogMessage(msg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        delegate.debug(marker, manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.debug(marker, manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        delegate.debug(marker, manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        delegate.debug(marker, getExceptionToLog(msg, t));
    }

    @Override
    public void debug(final String msg) {
        delegate.debug(manipulateLogMessage(msg));
    }

    /*
    * INFO level logging
    */

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(final String format, final Object arg) {
        delegate.info(manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        delegate.info(manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void info(final String format, final Object... arguments) {
        delegate.info(manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void info(final String msg, final Throwable t) {
        delegate.info(getExceptionToLog(msg, t));
    }

    @Override
    public void info(final Marker marker, final String msg) {
        delegate.info(marker, manipulateLogMessage(msg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        delegate.info(marker, manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.info(marker, manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        delegate.info(marker, manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        delegate.info(marker, getExceptionToLog(msg, t));
    }

    @Override
    public void info(final String msg) {
        delegate.info(manipulateLogMessage(msg));
    }

    /*
    * WARN level logging
    */

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(final String format, final Object arg) {
        delegate.warn(manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        delegate.warn(manipulateLogMessage(format),  manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        delegate.warn(manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        delegate.warn(getExceptionToLog(msg, t));
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        delegate.warn(marker, manipulateLogMessage(msg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        delegate.warn(marker, manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.warn(marker, manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        delegate.warn(marker, manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        delegate.warn(marker, getExceptionToLog(msg, t));
    }

    @Override
    public void warn(final String msg) {
        delegate.warn(manipulateLogMessage(msg));
    }

    /*
    * ERROR level logging
    */

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(final String format, final Object arg) {
        delegate.error(manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        delegate.error(manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void error(final String format, final Object... arguments) {
        delegate.error(manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void error(final String msg, final Throwable t) {
        delegate.error(getExceptionToLog(msg, t));
    }

    @Override
    public void error(final Marker marker, final String msg) {
        delegate.error(marker, manipulateLogMessage(msg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        delegate.error(marker, manipulateLogMessage(format), manipulateLogArguments(arg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        delegate.error(marker, manipulateLogMessage(format), manipulateLogArguments(arg1, arg2));
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        delegate.error(marker, manipulateLogMessage(format), manipulateLogArguments(arguments));
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        delegate.error(marker, getExceptionToLog(msg, t));
    }

    @Override
    public void error(final String msg) {
        delegate.error(manipulateLogMessage(msg));
    }

    @Override
    public String getName() {
        return "CAS Delegating Logger";
    }
}
