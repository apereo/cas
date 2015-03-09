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
package org.jasig.cas.web.support;

import org.jasig.cas.authentication.RememberMeCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.jasig.cas.web.support.CookieRetrievingCookieGenerator}.
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public final class CookieRetrievingCookieGeneratorTests {

    private CookieRetrievingCookieGenerator generator;

    @Before
    public void setUp() throws Exception {
        this.generator = new CookieRetrievingCookieGenerator();
        this.generator.setRememberMeMaxAge(100);
        this.generator.setCookieDomain("cas.org");
        this.generator.setCookieMaxAge(5);
        this.generator.setCookiePath("/");
        this.generator.setCookieName("test");
    }

    @Test
    public void verifyCookieAddWithRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.generator.addCookie(request, response, "test");
        request.setCookies(response.getCookies());

        final Cookie c = response.getCookie("test");
        assertEquals(100, c.getMaxAge());
        assertEquals("test", this.generator.retrieveCookieValue(request));
    }

    @Test
    public void verifyCookieAddWithoutRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.generator.addCookie(request, response, "test");
        request.setCookies(response.getCookies());

        final Cookie c = response.getCookie("test");
        assertEquals(5, c.getMaxAge());
        assertEquals("test", this.generator.retrieveCookieValue(request));
    }

    @Test
    public void verifyCookieRetrieve() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.generator.addCookie(request, response, "test");
        request.setCookies(response.getCookies());
        assertEquals("test", this.generator.retrieveCookieValue(request));
    }
}
