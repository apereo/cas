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
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.joda.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link SerialUtils#writeObject(Object, java.io.ObjectOutput)}
 * and {@link SerialUtils#readObject(Class, java.io.ObjectInput)}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class SerialUtilsObjectTests {

    private final Object object;

    public SerialUtilsObjectTests(final Object object) {
        this.object = object;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {

        return Arrays.asList(new Object[][] {
                { null },
                { "jabberwocky" },
                { (byte) 127 },
                { (short) 255 },
                { 31415 },
                { 3141592654L },
                { 3.14159f },
                { 3.141592654d },
                // { new AtomicInteger(139) }, // AtomicInteger does not implement custom equals needed for tests
                { new BigInteger("1234567898765432101011011") },
                { new Date()},
                { new Instant()},
                { new URL("ftp://fuzzy@alpha.uu.net/")},
                { new URI("http://www.example.com/page#anchor")},
        });
    }

    @Test
    public void testReadWrite() throws Exception {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(outBuffer);
        final Class<?> clazz = this.object == null ? Void.TYPE : this.object.getClass();
        try {
            SerialUtils.writeObject(this.object, out);
        } finally {
            out.close();
        }
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outBuffer.toByteArray()));
        final Object deserialized;
        try {
            deserialized = SerialUtils.readObject(clazz, in);
        } finally {
            in.close();
        }
        assertEquals(this.object, deserialized);
    }
}
