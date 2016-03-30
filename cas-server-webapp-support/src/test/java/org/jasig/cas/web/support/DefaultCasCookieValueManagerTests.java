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

import org.jasig.cas.util.BaseStringCipherExecutor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;

/**
 * Test cases for {@link DefaultCasCookieValueManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultCasCookieValueManagerTests {

    private static final String ENC_KEY = "1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM";
    private static final String SIGN_KEY = "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w";

    @Test(expected = IllegalStateException.class)
    public void defaultCookieWithNoRemote() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(null);
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        mgmr.buildCookieValue("cas", request);
    }

    @Test(expected = IllegalStateException.class)
    public void defaultCookieWithNoAgent() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        mgmr.buildCookieValue("cas", request);
    }

    @Test
    public void defaultCookieGood() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-agent", "the agent");
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        assertNotNull(mgmr.buildCookieValue("cas", request));
    }

    @Test
    public void defaultCookieVerify() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-agent", "the agent");
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        final String c = mgmr.buildCookieValue("cas", request);
        assertEquals("cas", mgmr.obtainCookieValue(new Cookie("test", c), request));
    }

    @Test(expected = IllegalStateException.class)
    public void defaultCookieVerifyNoRemote() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-agent", "the agent");
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        final String c = mgmr.buildCookieValue("cas", request);
        request.setRemoteAddr("another ip");
        assertEquals("cas", mgmr.obtainCookieValue(new Cookie("test", c), request));
    }

    @Test(expected = IllegalStateException.class)
    public void defaultCookieVerifyNoAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-agent", "the agent");
        final DefaultCasCookieValueManager mgmr = new DefaultCasCookieValueManager(new BaseStringCipherExecutor(ENC_KEY, SIGN_KEY));
        final String c = mgmr.buildCookieValue("cas", request);

        request = new MockHttpServletRequest();
        request.addHeader("user-agent", "something else");
        assertEquals("cas", mgmr.obtainCookieValue(new Cookie("test", c), request));
    }

}
