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

package org.jasig.cas.util;

import org.jasig.cas.TestUtils;
import org.jasig.cas.mock.MockServiceTicket;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.ticket.Ticket;
import org.junit.Test;

/**
 * Test cases for {@link CompressionUtils}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CompressionUtilsTests {

    private final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
    private final JCEBasedCipherExecutor cipher = new JCEBasedCipherExecutor("1234567890123456", "1234567890123456");

    @Test
    public void testSerializationOfTgt() {
        final byte[] bytes = CompressionUtils.serializeAndEncodeObject(cipher, tgt);
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
    }

    @Test
    public void testSerializationOfSt() {
        final MockServiceTicket st = new MockServiceTicket("serviceid", TestUtils.getService(), tgt);
        final byte[] bytes = CompressionUtils.serializeAndEncodeObject(cipher, st);
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
    }
}
