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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test for {@link AttributeMapSerializer} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class AttributeMapSerializerTests {
    private final Log logger = LogFactory.getLog(getClass());
  
    private final Map<String, Object> attributes;
    
    public AttributeMapSerializerTests(final Map<String, Object> attributes) {
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
