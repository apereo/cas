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

import com.google.common.io.ByteSource;
import org.jasig.cas.TestUtils;
import org.jasig.cas.mock.MockServiceTicket;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.ticket.Ticket;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link CompressionUtils}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CompressionUtilsTests {

    private final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
    private final ShiroCipherExecutor cipher = new ShiroCipherExecutor("1234567890123456",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");

    @Test
    public void testSerializationOfTgt() {
        final byte[] bytes = CompressionUtils.serializeAndEncodeObject(cipher, tgt);
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void testSerializationOfSt() {
        final MockServiceTicket st = new MockServiceTicket("serviceid", TestUtils.getService(), tgt);
        final byte[] bytes = CompressionUtils.serializeAndEncodeObject(cipher, st);
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void testSerializationOfStBase64Encode() {
        final MockServiceTicket st = new MockServiceTicket("serviceid", TestUtils.getService(), tgt);
        final byte[] bytes = CompressionUtils.serializeAndEncodeObject(cipher, st);
        final String string = CompressionUtils.encodeBase64(bytes);
        assertNotNull(string);
        final byte[] result = CompressionUtils.decodeBase64(string);
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(result, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void testSerializationOfTgtByteSource() throws Exception {
        final ByteSource bytes = ByteSource.wrap(CompressionUtils.serializeAndEncodeObject(cipher, tgt));
        final Ticket obj = CompressionUtils.decodeAndSerializeObject(bytes.read(), cipher, Ticket.class);
        assertNotNull(obj);
    }

}
