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
package org.jasig.cas.ticket.registry.support.kryo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link KryoTranscoder} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class KryoTranscoderTests {

    private final KryoTranscoder transcoder;
    
    public KryoTranscoderTests(final int bufferSize) {
        transcoder = new KryoTranscoder(bufferSize);
        transcoder.setSerializerMap(Collections.<Class<?>, Serializer>singletonMap(
                MockServiceTicket.class,
                new FieldSerializer(transcoder.getKryo(), MockServiceTicket.class)));
        transcoder.initialize();
    }

    @Parameterized.Parameters
    public static List<Object[]> getTestParms() {
        final List<Object[]> params = new ArrayList<Object[]>(6);

        // Test case #1 - Buffer is bigger than encoded data
        params.add(new Object[] {1024});

        // Test case #2 - Buffer overflow case
        params.add(new Object[] {10});
        return params;
    }

    @Test
    public void testEncodeDecode() throws Exception {
        final ServiceTicket expected =
                new MockServiceTicket("ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK");
        assertEquals(expected, transcoder.decode(transcoder.encode(expected)));
    }
    
    static class MockServiceTicket implements ServiceTicket {
        private String id;

        MockServiceTicket() { /* for serialization */ }

        public MockServiceTicket(final String id) {
            this.id = id;
        }

        public Service getService() {
            return null;
        }

        public boolean isFromNewLogin() {
            return false;
        }

        public boolean isValidFor(Service service) {
            return false;
        }

        public TicketGrantingTicket grantTicketGrantingTicket(String id, Authentication authentication, ExpirationPolicy expirationPolicy) {
            return null;
        }

        public String getId() {
            return id;
        }

        public boolean isExpired() {
            return false;
        }

        public TicketGrantingTicket getGrantingTicket() {
            return null;
        }

        public long getCreationTime() {
            return 0;
        }

        public int getCountOfUses() {
            return 0;
        }
        
        public boolean equals(final Object other) {
            return other instanceof MockServiceTicket && ((MockServiceTicket) other).getId().equals(id);
        }
    }
}
