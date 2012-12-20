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
package org.jasig.cas.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jasig.cas.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link SerialUtils#writeMap(java.util.Map, java.io.ObjectOutput)} and
 * {@link SerialUtils#readMap(Class, Class, java.io.ObjectInput)}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(org.junit.runners.Parameterized.class)
public class SerialUtilsMapTests {

    private final Map<?, ?> map;
    private final Class<?> keyClass;
    private final Class<?> valueClass;

    public SerialUtilsMapTests(final Map<?, ?> map, final Class<?> keyClass, final Class<?> valueClass) {
        this.map = map;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {
        // Test case #1
        // Specialized empty map
        final Map<String, Object> map1 = Collections.emptyMap();

        // Test case #2
        // Hash map with value that is subclass of container type
        final Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("Foo", new URI("http://www.google.com"));

        // Test case #3
        // Linked hash map with null keys and values
        final Map<String, Byte> map3 = new LinkedHashMap<String, Byte>();
        map3.put("b", (byte) 2);
        map3.put(null, (byte) 1);
        map3.put("c", null);

        // Test case #4
        // Tree map
        final Map<String, Message> map4 = new TreeMap<String, Message>();
        map4.put("gamma", null);
        map4.put("alpha", new Message("a", "alpha"));
        map4.put("beta", new Message("b", "beta"));

        return Arrays.asList(new Object[][] {
                { map1, String.class, Object.class },
                { map2, String.class, Object.class },
                { map3, String.class, Byte.class },
                { map4, String.class, Message.class },
        });
    }

    @Test
    public void testReadWrite() throws Exception {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(outBuffer);
        try {
            SerialUtils.writeMap(this.map, out);
        } finally {
            out.close();
        }
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outBuffer.toByteArray()));
        final Map<?, ?> deserialized;
        try {
            deserialized = SerialUtils.readMap(this.keyClass, this.valueClass, in);
        } finally {
            in.close();
        }
        assertEquals(this.map, deserialized);
    }
}
