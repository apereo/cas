/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link JCEBasedCipherExecutor}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class JCEBasedCipherExecutorTests {

    @Test
    public void testEncodingDecoding() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new JCEBasedCipherExecutor("1234567890123456",
                "1234567890123456",
                "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        final byte[] bytes = cc.encode(value.getBytes());
        final byte[] decoded = cc.decode(bytes);
        assertEquals(new String(decoded), value);
    }

    @Test(expected=RuntimeException.class)
    public void testEncodingDecodingBadKeys() {
        final String value = "ThisIsATestValueThatIsGoingToBeEncodedAndDecodedAgainAndAgain";
        final CipherExecutor<byte[], byte[]> cc = new JCEBasedCipherExecutor("0000", "9999", "1234");
        cc.encode(value.getBytes());
    }
}
