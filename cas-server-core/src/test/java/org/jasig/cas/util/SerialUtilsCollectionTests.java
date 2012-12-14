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
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link org.jasig.cas.util.SerialUtils#writeCollection(java.util.Collection, java.io.ObjectOutput)}
 * and {@link org.jasig.cas.util.SerialUtils#readCollection(Class, java.io.ObjectInput)}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class SerialUtilsCollectionTests {

    private Collection<?> collection;
    private Class<?> itemClass;

    public SerialUtilsCollectionTests(final Collection<?> collection, final Class<?> itemClass) {
        this.collection = collection;
        this.itemClass = itemClass;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {
        // Test case #1
        // Specialized empty list
        final Collection<String> collection1 = Collections.emptyList();

        // Test case #2
        // Specialized empty set
        final Collection<String> collection2 = Collections.emptySet();

        // Test case #3
        // Specialized singleton set
        final Collection<String> collection3 = Collections.singleton("aleph");

        // Test case #4
        // Specialized singleton list
        final Collection<String> collection4 = Collections.singletonList("bet");

        // Test case #5
        // ArrayList with null values
        final Collection<URL> collection5 = new ArrayList<URL>();
        collection5.add(new URL("http:/www.google.com"));
        collection5.add(new URL("https:/mail.google.com/u/1"));
        collection5.add(null);

        // Test case #6
        // HashSet with null values
        final Collection<String> collection6 = new HashSet<String>();
        collection6.add("a");
        collection6.add("b");
        collection6.add(null);

        // Test case #7
        // TreeSet
        final Collection<BigInteger> collection7 = new TreeSet<BigInteger>();
        collection7.add(new BigInteger("123"));
        collection7.add(new BigInteger("456"));
        collection7.add(new BigInteger("789"));

        return Arrays.asList(new Object[][] {
                { collection1, String.class },
                { collection2, String.class },
                { collection3, String.class },
                { collection4, String.class },
                { collection5, URL.class },
                { collection6, String.class },
                { collection7, BigInteger.class },
        });
    }

    @Test
    public void testReadWrite() throws Exception {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(outBuffer);
        try {
            SerialUtils.writeCollection(this.collection, out);
        } finally {
            out.close();
        }
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outBuffer.toByteArray()));
        final Collection<?> deserialized;
        try {
            deserialized = SerialUtils.readCollection(this.itemClass, in);
        } finally {
            in.close();
        }
        assertEquals(this.collection, deserialized);
    }
}
