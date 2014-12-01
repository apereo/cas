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
package org.jasig.cas.support.saml.web.support;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class WebUtilTests {

    @Test
    public void verifyFindService() {
        final SamlArgumentExtractor openIdArgumentExtractor = new SamlArgumentExtractor();
        final CasArgumentExtractor casArgumentExtractor = new CasArgumentExtractor();
        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
                openIdArgumentExtractor, casArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Arrays
                .asList(argumentExtractors), request);

        assertEquals("test", service.getId());
    }

    @Test
    public void verifyFoundNoService() {
        final SamlArgumentExtractor openIdArgumentExtractor = new SamlArgumentExtractor();
        final ArgumentExtractor[] argumentExtractors = new ArgumentExtractor[] {
                openIdArgumentExtractor};
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");

        final Service service = WebUtils.getService(Arrays
                .asList(argumentExtractors), request);

        assertNull(service);
    }
}
