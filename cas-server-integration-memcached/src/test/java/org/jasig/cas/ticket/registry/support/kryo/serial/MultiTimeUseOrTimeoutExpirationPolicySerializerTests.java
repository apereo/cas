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
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.nio.ByteBuffer;

import junit.framework.Assert;
import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link MultiTimeUseOrTimeoutExpirationPolicySerializer} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MultiTimeUseOrTimeoutExpirationPolicySerializerTests {
    @Test
    public void testReadWrite() throws Exception {
        final MultiTimeUseOrTimeoutExpirationPolicy expected = new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000);
        final MultiTimeUseOrTimeoutExpirationPolicySerializer serialzer =
                new MultiTimeUseOrTimeoutExpirationPolicySerializer(new FieldHelper());
       
        final ByteBuffer buffer = ByteBuffer.allocate(128);
        serialzer.write(buffer, expected);
        buffer.flip();
        final MultiTimeUseOrTimeoutExpirationPolicy actual = serialzer.read(buffer);
        final TicketState ticket = mock(TicketState.class);
        when(ticket.getLastTimeUsed()).thenReturn(System.currentTimeMillis() - 5001);
        Assert.assertEquals(expected.isExpired(ticket), actual.isExpired(ticket));
    }
}
