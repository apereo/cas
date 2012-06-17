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

package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.4
 */
public class ResponseTests extends TestCase {

     public void testConstructionWithoutFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals(url + "?ticket=foobar", response.getUrl());
    }

    public void testConstructionWithoutFragmentButHasQueryString() {
        final String url = "http://localhost:8080/foo?test=boo";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals(url + "&ticket=foobar", response.getUrl());
    }

    public void testConstructionWithFragmentAndQueryString() {
        final String url = "http://localhost:8080/foo?test=boo#hello";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?test=boo&ticket=foobar#hello", response.getUrl());
    }

    public void testConstructionWithFragmentAndNoQueryString() {
        final String url = "http://localhost:8080/foo#hello";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "foobar");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("http://localhost:8080/foo?ticket=foobar#hello", response.getUrl());

    }

    public void testUrlSanitization() {
        final String url = "https://www.example.com\r\nLocation: javascript:\r\n\r\n<script>alert(document.cookie)</script>";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "ST-12345");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com Location: javascript: <script>alert(document.cookie)</script>?ticket=ST-12345", response.getUrl());
    }

    public void testUrlWithUnicode() {
        final String url = "https://www.example.com/πολιτικῶν";
        final Map<String, String> attributes = new HashMap<String,String>();
        attributes.put("ticket", "ST-12345");
        final Response response = Response.getRedirectResponse(url, attributes);
        assertEquals("https://www.example.com/πολιτικῶν?ticket=ST-12345", response.getUrl());
    }
}
