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

import org.jasig.cas.support.openid.AbstractOpenIdTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;


/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdPostUrlHandlerMappingTests extends AbstractOpenIdTests {

    @Autowired
    private OpenIdPostUrlHandlerMapping handlerMapping;


    @Test
    public void verifyNoMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/hello", request));
    }

    @Test
    public void verifyImproperMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchWrongMethod() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("GET");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchCorrectMethodNoParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    @Test
    public void verifyProperMatchCorrectMethodWithParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");


        assertNotNull(this.handlerMapping.lookupHandler("/login", request));
    }
}
