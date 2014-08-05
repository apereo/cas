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

import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertFalse;

/**
 * @author Misagh Moayyed
 */
public class TicketObfuscatingWrappingAppenderTests {

    @Test
    public void testTgtTicketIdsInLogs() {
        final TicketObfuscatingWrappingAppender appender = new TicketObfuscatingWrappingAppender();
        final StringWriter writer = new StringWriter();
        final WriterAppender appW =  new WriterAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), writer);
        appW.setName("STRING");
        appender.addAppender(appW);

        final DefaultUniqueTicketIdGenerator gen = new DefaultUniqueTicketIdGenerator(30, "host.name.edu");
        final String ticketId = gen.getNewTicketId(TicketGrantingTicket.PREFIX);

        final LoggingEvent event = new LoggingEvent(this.getClass().getName(),
                new RootLogger(Level.ALL), Level.ALL, ticketId, null);

        appender.doAppend(event);

        final String output = writer.getBuffer().toString();
        assertFalse(output.contains(ticketId));
    }

    @Test
    public void testPgtTicketIdsInLogs() {
        final TicketObfuscatingWrappingAppender appender = new TicketObfuscatingWrappingAppender();
        final StringWriter writer = new StringWriter();
        final WriterAppender appW =  new WriterAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), writer);
        appW.setName("STRING");
        appender.addAppender(appW);

        final DefaultUniqueTicketIdGenerator gen = new DefaultUniqueTicketIdGenerator(30, "host.name.edu");
        final String ticketId = gen.getNewTicketId(TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);

        final LoggingEvent event = new LoggingEvent(this.getClass().getName(),
                new RootLogger(Level.ALL), Level.ALL, ticketId, null);

        appender.doAppend(event);

        final String output = writer.getBuffer().toString();
        assertFalse(output.contains(ticketId));
    }
}
