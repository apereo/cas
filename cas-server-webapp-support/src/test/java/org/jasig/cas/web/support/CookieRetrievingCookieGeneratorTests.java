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
package org.jasig.cas.web.support;

import static org.junit.Assert.*;

import javax.servlet.http.Cookie;

import org.jasig.cas.authentication.RememberMeCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public final class CookieRetrievingCookieGeneratorTests {

    private CookieRetrievingCookieGenerator g;

    @Before
    public void setUp() throws Exception {
        this.g = new CookieRetrievingCookieGenerator();
        this.g.setRememberMeMaxAge(100);
        this.g.setCookieDomain("cas.org");
        this.g.setCookieMaxAge(5);
        this.g.setCookiePath("/");
        this.g.setCookieName("test");
    }

    @Test
    public void testCookieAddWithRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.g.addCookie(request, response, "test");

        final Cookie c = response.getCookie("test");
        assertEquals(100, c.getMaxAge());
        assertEquals("test", c.getValue());
    }

    @Test
    public void testCookieAddWithoutRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        this.g.addCookie(request, response, "test");

        final Cookie c = response.getCookie("test");
        assertEquals(5, c.getMaxAge());
        assertEquals("test", c.getValue());
    }

    @Test
    public void testCookieRetrieve() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Cookie cookie = new Cookie("test", "test");
        cookie.setDomain("cas.org");
        cookie.setMaxAge(5);
        request.setCookies(new Cookie[] {cookie});

        assertEquals("test", this.g.retrieveCookieValue(request));
    }
}
