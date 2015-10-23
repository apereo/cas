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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGeneratorTests {

    @Test
    public void verifyGenerator() {
        final ShibbolethCompatiblePersistentIdGenerator generator =
                new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        final Principal p = TestUtils.getPrincipal();
        final String value = generator.generate(p, TestUtils.getService());

        assertNotNull(value);
    }

}
