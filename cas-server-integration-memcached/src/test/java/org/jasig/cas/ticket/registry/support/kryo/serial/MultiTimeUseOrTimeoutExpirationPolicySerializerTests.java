/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package org.jasig.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Description of MultiTimeUseOrTimeoutExpirationPolicySerializerTests.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class MultiTimeUseOrTimeoutExpirationPolicySerializerTests {
    @Test
    public void testReadWrite() throws Exception {
        final MultiTimeUseOrTimeoutExpirationPolicy expected = new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000);
        final MultiTimeUseOrTimeoutExpirationPolicySerializer serialzer =
                new MultiTimeUseOrTimeoutExpirationPolicySerializer(new Kryo(), new FieldHelper());
       
        final ByteBuffer buffer = ByteBuffer.allocate(128);
        serialzer.write(buffer, expected);
        buffer.flip();
        final MultiTimeUseOrTimeoutExpirationPolicy actual = serialzer.read(buffer);
    }
}
