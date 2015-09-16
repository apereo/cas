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

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdPostUrlHandlerMappingTests {

    private OpenIdPostUrlHandlerMapping handlerMapping;

    public void verifyTest() {
    }

    /*
    protected void setUp() throws Exception {
        final GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.refresh();
        final RootBeanDefinition definition = new RootBeanDefinition(Object.class);
        context.registerBeanDefinition("testHandler", definition);

        context.start();

        final Map<String, Object> properties = new HashMap<>();
        properties.put("/login", new Object());

        this.handlerMapping = new OpenIdPostUrlHandlerMapping();
        this.handlerMapping.setUrlMap(properties);

        this.handlerMapping.initApplicationContext();
    }


    public void verifyNoMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/hello", request));
    }

    public void verifyImproperMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    public void verifyProperMatchWrongMethod() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("GET");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    public void verifyProperMatchCorrectMethodNoParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");

        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }

    public void verifyProperMatchCorrectMethodWithParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");


        assertNotNull(this.handlerMapping.lookupHandler("/login", request));
    }*/
}
