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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class DefaultLongNumericGeneratorTests {

    @Test
    public void testWrap() {
        assertEquals(Long.MAX_VALUE, new DefaultLongNumericGenerator(Long.MAX_VALUE)
            .getNextLong());
    }

    @Test
    public void testInitialValue() {
        assertEquals(10L, new DefaultLongNumericGenerator(10L)
            .getNextLong());
    }

    @Test
    public void testIncrementWithNoWrap() {
        assertEquals(0, new DefaultLongNumericGenerator().getNextLong());
    }

    @Test
    public void testIncrementWithNoWrap2() {
        final DefaultLongNumericGenerator g = new DefaultLongNumericGenerator();
        g.getNextLong();
        assertEquals(1, g.getNextLong());
    }

    @Test
    public void testMinimumSize() {
        assertEquals(1, new DefaultLongNumericGenerator().minLength());
    }

    @Test
    public void testMaximumLength() {
        assertEquals(Long.toString(Long.MAX_VALUE).length(),
            new DefaultLongNumericGenerator().maxLength());
    }
}
