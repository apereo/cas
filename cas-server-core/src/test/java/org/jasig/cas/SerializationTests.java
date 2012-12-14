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
package org.jasig.cas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.Principal;
import org.jasig.cas.authentication.SimplePrincipal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that Serializable classes are equal under serialization.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class SerializationTests {

    private Object object;

    public SerializationTests(final Object object) {
        this.object = object;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {

        final Principal principalWithoutAttributes = new SimplePrincipal("desert");
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("a", "alpha");
        attributes.put("b", "beta");
        attributes.put("c", "gamma");
        final Principal principalWithAttributes = new SimplePrincipal("greeks", attributes);
        return Arrays.asList(new Object[][] {
                { principalWithoutAttributes },
                { principalWithAttributes },
                { TestUtils.newMutableAuthentication(principalWithoutAttributes) },
                { TestUtils.newMutableAuthentication(principalWithAttributes) },
                { TestUtils.newImmutableAuthentication(principalWithoutAttributes) },
                { TestUtils.newImmutableAuthentication(principalWithAttributes) },
        });
    }

    @Test
    public void testReadWrite() throws Exception {
        final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(outBuffer);
        final Class<?> clazz = this.object == null ? Void.TYPE : this.object.getClass();
        try {
            out.writeObject(this.object);
        } finally {
            out.close();
        }
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outBuffer.toByteArray()));
        final Object deserialized;
        try {
            deserialized = in.readObject();
        } finally {
            in.close();
        }
        assertEquals(this.object, deserialized);
    }
}
