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

package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;


import static org.junit.Assert.*;

/**
 * Test cases for {@link SamlServiceFactory}
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlServiceFactoryTests {
    @Test
    public void verifyObtainService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");

        final SamlServiceFactory factory = new SamlServiceFactory();
        final Service service = factory.createService(request);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyServiceDoesNotExist() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final SamlServiceFactory factory = new SamlServiceFactory();
        assertNull(factory.createService(request));
    }
}
