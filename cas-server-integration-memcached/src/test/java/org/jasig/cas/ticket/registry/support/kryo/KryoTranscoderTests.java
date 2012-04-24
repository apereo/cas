/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
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

    private KryoTranscoder transcoder;
    
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
