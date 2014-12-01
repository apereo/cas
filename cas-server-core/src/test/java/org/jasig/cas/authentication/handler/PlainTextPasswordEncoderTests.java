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
package org.jasig.cas.authentication.handler;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class PlainTextPasswordEncoderTests {

    private static final String CONST_TO_ENCODE = "CAS IS COOL";

    private final PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    @Test
    public void verifyNullValueToTranslate() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    @Test
    public void verifyValueToTranslate() {
        assertEquals(CONST_TO_ENCODE, this.passwordEncoder.encode(CONST_TO_ENCODE));
    }
}
