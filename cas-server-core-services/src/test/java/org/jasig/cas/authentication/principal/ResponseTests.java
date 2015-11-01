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


import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.4.4
 */
public class ResponseTests {

    @Test
    public void verifyConstructionWithoutFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "foobar");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "?ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithoutFragmentButHasQueryString() {
        final String url = "http://localhost:8080/foo?test=boo";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "foobar");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals(url + "&ticket=foobar", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndQueryString() {
        final String url = "http://localhost:8080/foo?test=boo#hello";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "foobar");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?test=boo&ticket=foobar#hello", response.getUrl());
    }

    @Test
    public void verifyConstructionWithFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo#hello";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "foobar");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?ticket=foobar#hello", response.getUrl());

    }

    @Test
    public void verifyUrlSanitization() {
        final String url = "https://www.example.com\r\nLocation: javascript:\r\n\r\n<script>alert(document.cookie)</script>";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "ST-12345");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com Location: javascript: <script>alert(document.cookie)</script>?ticket=ST-12345",
                response.getUrl());
    }

    @Test
    public void verifyUrlWithUnicode() {
        final String url = "https://www.example.com/πολιτικῶν";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("ticket", "ST-12345");
        final Response response = DefaultResponse.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com/πολιτικῶν?ticket=ST-12345", response.getUrl());
    }
}
