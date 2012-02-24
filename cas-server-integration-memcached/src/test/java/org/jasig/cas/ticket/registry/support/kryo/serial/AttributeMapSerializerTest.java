package org.jasig.cas.ticket.registry.support.kryo.serial;/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/

import com.esotericsoftware.kryo.Kryo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Description of org.jasig.cas.ticket.registry.support.kryo.serial.AttributeMapSerializerTest.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class AttributeMapSerializerTest {
    private final Log logger = LogFactory.getLog(getClass());
  
    private Map<String, Object> attributes;
    
    public AttributeMapSerializerTest(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        final Collection<Object[]> params = new ArrayList<Object[]>();
        Map<String, Object> attrMap;

        // Test case #1: single-valued attribute
        attrMap = new LinkedHashMap<String, Object>() {{
            put("altUsername", "fhqwhgads");
        }};
        params.add(new Object[] { attrMap });

        // Test case #2: multi-valued attribute
        final List<String> names = new ArrayList<String>(3);
        names.add("strongbad");
        names.add("homsar");
        names.add("eh-steve");
        attrMap = new LinkedHashMap<String, Object>() {{
            put("altUsernames", names);
        }};
        params.add(new Object[] { attrMap });


        // Test case #3: null attribute
        attrMap = new LinkedHashMap<String, Object>() {{
            put("altUsername", null);
        }};
        params.add(new Object[] { attrMap });

        return params;
    }
    
    @Test
    public void testReadWrite() throws Exception {
        final Kryo kryo = new Kryo();
        kryo.register(ArrayList.class);
        final AttributeMapSerializer serializer = new AttributeMapSerializer(kryo);
        final ByteBuffer buffer = ByteBuffer.allocate(2048);
        serializer.write(buffer, this.attributes);
        buffer.flip();
        // Print the buffer to help w/debugging
        printBuffer(buffer);
        Assert.assertEquals(this.attributes, serializer.read(buffer));
    }
    
    private void printBuffer(final ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        try {
            logger.debug(new String(bytes, "UTF-8"));
        } catch (Exception e) {
            logger.error("Error printing buffer as string.");
        }
        buffer.rewind();
    }
}
