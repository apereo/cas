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
package org.jasig.cas.support.openid.web.support;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.1

 */
public class DefaultOpenIdUserNameExtractorTests {

    private final DefaultOpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();

    @Test
    public void verifyExtractionSuccessful() {
        assertEquals("scootman28", this.extractor
                .extractLocalUsernameFromUri("http://test.com/scootman28"));
    }

    @Test
    public void verifyExtractionFailed() {
        assertNull(this.extractor
                .extractLocalUsernameFromUri("test.com"));
    }

    @Test
    public void verifyNull() {
        assertNull(this.extractor
                .extractLocalUsernameFromUri(null));
    }
}
