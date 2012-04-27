/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
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
