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

package org.jasig.cas.util.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Wraps other appends inside itself, and modifies the log
 * message before it's passed onto other appenders.
 * The log message is strictly examines for sensitive
 * data such as ticket granting ticket ids, etc and
 * is encoded.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class TicketEncodingWrappingAppender extends AppenderSkeleton implements AppenderAttachable {

    private final List<Appender> appenders = new ArrayList<Appender>();
    @Override
    public final void close() {
        synchronized (appenders) {
            for (Appender appender : appenders) {
                appender.close();
            }
        }
    }

    @Override
    public final boolean requiresLayout() {
        return false;
    }

    @Override
    public final void addAppender(final Appender appender) {
        synchronized (appenders) {
            appenders.add(appender);
        }
    }

    @Override
    public final Enumeration getAllAppenders() {
        return Collections.enumeration(appenders);
    }

    @Override
    public final Appender getAppender(final String s) {
        synchronized (appenders) {
            for (final Appender appender : appenders) {
                if (appender.getName().equals(name)) {
                    return appender;
                }
            }
        }
        return null;
    }

    @Override
    public final boolean isAttached(final Appender appender) {
        synchronized (appenders) {
            for (final Appender wrapped : appenders) {
                if (wrapped.equals(appender)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public final void removeAllAppenders() {
        synchronized (appenders) {
            appenders.clear();
        }
    }

    @Override
    public final void removeAppender(final Appender appender) {
        synchronized (appenders) {
            for (final Iterator<Appender> i = appenders.iterator(); i.hasNext();) {
                if (i.next().equals(appender)) {
                    i.remove();
                }
            }
        }
    }

    @Override
    public final void removeAppender(final String s) {
        synchronized (appenders) {
            for (final Iterator<Appender> i = appenders.iterator(); i.hasNext();) {
                if (i.next().getName().equals(name)) {
                    i.remove();
                }
            }
        }
    }

    @Override
    protected final void append(final LoggingEvent event) {
        final LoggingEvent eventToUse = modifyLoggingEventIfNeeded(event);
        synchronized (appenders) {
            for (final Appender appender : appenders) {
                appender.doAppend(eventToUse);
            }
        }
    }

    /**
     * Modify logging event if needed. Examines the
     * log message to remove/encode sensitive data.
     *
     * @param event the event
     * @return the new logging event
     */
    protected LoggingEvent modifyLoggingEventIfNeeded(final LoggingEvent event) {
       final String modifiedMessage = removeTicketGrantingTicketFromMessage(event.getMessage().toString());

        final LoggingEvent modifiedEvent = new LoggingEvent(event.getFQNOfLoggerClass(),
                event.getLogger(),
                event.getTimeStamp(),
                event.getLevel(),
                event.getMessage().toString(),
                event.getThreadName(),
                event.getThrowableInformation(),
                event.getNDC(),
                event.getLocationInformation(),
                event.getProperties());
        return modifiedEvent;
    }

    /**
     * Remove ticket granting ticket from the log message.
     *
     * @param modifiedMessage the modified message
     * @return the modified message with tgt id removed
     */
    private String removeTicketGrantingTicketFromMessage(final String modifiedMessage) {
        if (modifiedMessage.contains(TicketGrantingTicket.PREFIX)) {
            final int ticketPrefixIndex = modifiedMessage.indexOf(TicketGrantingTicket.PREFIX);
            int endingIndex = ticketPrefixIndex;
            boolean found = false;

            while (!found && endingIndex < modifiedMessage.length()) {
                final char c = modifiedMessage.charAt(endingIndex);
                if (c == ' ') {
                    found = true;
                } else {
                    endingIndex++;
                }
            }
            final String ticketId = modifiedMessage.substring(ticketPrefixIndex, endingIndex);
            return modifiedMessage.replace(ticketId,
                    TicketGrantingTicket.PREFIX.concat("-*************"));
        }
        return modifiedMessage;
    }
}
