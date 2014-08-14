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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Misagh Moayyed
 */
public class CasLoggerFactoryTests {
    private static Logger LOGGER;

    private static final String ID1 = "TGT-1-B0tjWgMIhUU4kgCZdXbxnWccTFYpTbRbArjaoutXnlNMbIShEu-cas";
    private static final String ID2 = "PGT-1-B0tjWgMIhUU4kgCZd32xnWccTFYpTbRbArjaoutXnlNMbIShEu-cas";

    private static final PrintStream OUT = System.out;

    private LoggedPrintStream loggedPrintStream;

    @After
    public void after() {
        System.out.flush();
        System.setOut(OUT);
        System.setErr(OUT);
    }

    @Before
    public void beforeTest() {
        System.out.flush();
        loggedPrintStream = LoggedPrintStream.create(System.out);
        System.setOut(loggedPrintStream);

        LOGGER = LoggerFactory.getLogger(CasLoggerFactoryTests.class);
    }

    @Test
    public void testLoggerSelectedCorrectly() {
        assertTrue(LOGGER instanceof CasDelegatingLogger);
    }

    @Test
    public void testLogging1() {
        LOGGER.trace(mock(Marker.class), getMessageToLogWithParams(), null, null);
        validateLogData();
    }

    @Test
    public void testLogging2() {
        LOGGER.trace(mock(Marker.class), getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging3() {
        LOGGER.trace(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging4() {
        LOGGER.trace(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging5() {
        LOGGER.trace(mock(Marker.class), getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging6() {
        LOGGER.trace(getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging7() {
        LOGGER.trace(getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging8() {
        LOGGER.trace(getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging9() {
        LOGGER.trace(getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging21() {
        LOGGER.debug(mock(Marker.class), getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging31() {
        LOGGER.debug(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging41() {
        LOGGER.debug(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging51() {
        LOGGER.debug(mock(Marker.class), getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging61() {
        LOGGER.debug(getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging771() {
        final TicketGrantingTicket t = mock(TicketGrantingTicket.class);
        when(t.getId()).thenReturn(ID1);
        when(t.toString()).thenReturn(ID1);

        LOGGER.debug(getMessageToLogWithParams(), ID2, t);
        validateLogData();
    }

    @Test
    public void testLogging71() {
        LOGGER.debug(getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging81() {
        LOGGER.debug(getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging91() {
        LOGGER.debug(getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging211() {
        LOGGER.info(mock(Marker.class), getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging311() {
        LOGGER.info(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging411() {
        LOGGER.info(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging511() {
        LOGGER.info(mock(Marker.class), getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging611() {
        LOGGER.info(getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging711() {
        LOGGER.info(getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging811() {
        LOGGER.info(getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging911() {
        LOGGER.info(getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging2111() {
        LOGGER.warn(mock(Marker.class), getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging3111() {
        LOGGER.warn(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging4111() {
        LOGGER.warn(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging5111() {
        LOGGER.warn(mock(Marker.class), getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging6111() {
        LOGGER.warn(getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging7111() {
        LOGGER.warn(getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging8111() {
        LOGGER.warn(getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging9111() {
        LOGGER.warn(getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging21110() {
        LOGGER.error(mock(Marker.class), getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging31110() {
        LOGGER.error(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging41110() {
        LOGGER.error(mock(Marker.class), getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging51110() {
        LOGGER.error(mock(Marker.class), getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    @Test
    public void testLogging61110() {
        LOGGER.error(getMessageToLog());
        validateLogData();
    }

    @Test
    public void testLogging71110() {
        LOGGER.error(getMessageToLogWithParams(), ID2, ID1);
        validateLogData();
    }

    @Test
    public void testLogging81110() {
        LOGGER.error(getMessageToLogWithParams(), ID2, ID1, ID2);
        validateLogData();
    }

    @Test
    public void testLogging91110() {
        LOGGER.error(getMessageToLog(), new RuntimeException(ID1, new InvalidTicketException(ID2)));
        validateLogData();
    }

    private String getMessageToLog() {
        return String.format("Here is one %s and here is another %s", ID1, ID2);
    }

    private String getMessageToLogWithParams() {
        return "Here is one {} and here is another {}";
    }

    private void validateLogData() {
        final String data = this.loggedPrintStream.getBuffer();
        assertTrue(StringUtils.isNotBlank(data));
        assertFalse(data.contains(ID1));
        assertFalse(data.contains(ID2));
    }

    private static class LoggedPrintStream extends PrintStream {

        private static StringBuilder BUFFER = new StringBuilder();

        private final PrintStream underlying;

        public LoggedPrintStream(final OutputStream os, final PrintStream ul) {
            super(os);
            this.underlying = ul;
        }

        public static LoggedPrintStream create(final PrintStream toLog) {
            try {
                final StringBuilder sb = new StringBuilder();
                final Field f = FilterOutputStream.class.getDeclaredField("out");
                f.setAccessible(true);
                final OutputStream psout = (OutputStream) f.get(toLog);
                return new LoggedPrintStream(new FilterOutputStream(psout) {
                    public void write(final int b) throws IOException {
                        super.write(b);
                        final char c = (char) b;
                        BUFFER.append(c);
                    }
                }, toLog);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void flush() {
            super.flush();

        }

        public String getBuffer() {
            final String buffer = BUFFER.toString();
            BUFFER = new StringBuilder();
            return buffer;
        }

        public PrintStream getOriginal() {
            return this.underlying;
        }
    }
}

