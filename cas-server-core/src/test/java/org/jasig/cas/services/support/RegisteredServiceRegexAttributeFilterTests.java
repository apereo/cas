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
package org.jasig.cas.services.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

/**
 * @author Misagh Moayyed
 * @since 4.0
 */
public class RegisteredServiceRegexAttributeFilterTests {

    private RegisteredServiceAttributeFilter filter;
    private Map<String, Object> givenAttributesMap = null;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceRegexAttributeFilterTests() {

        this.filter = new RegisteredServiceRegexAttributeFilter("^.{5,}$");

        this.givenAttributesMap = new HashMap<String, Object>();
        this.givenAttributesMap.put("uid", "loggedInTestUid");
        this.givenAttributesMap.put("phone", "1290");
        this.givenAttributesMap.put("familyName", "Smith");
        this.givenAttributesMap.put("givenName", "John");
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry"));
        this.givenAttributesMap.put("arrayAttribute", new String[] {"math", "science", "chemistry"});
        this.givenAttributesMap.put("setAttribute", new HashSet<String>(Arrays.asList("math", "science", "chemistry")));

        final Map<String, String> mapAttributes = new HashMap<String, String>();
        mapAttributes.put("uid", "loggedInTestUid");
        mapAttributes.put("phone", "890");
        mapAttributes.put("familyName", "Smith");
        this.givenAttributesMap.put("mapAttribute", mapAttributes);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.jasig.org");
        when(this.registeredService.getAllowedAttributes()).thenReturn(
                Arrays.asList("givenName", "uid", "phone", "memberOf", "mapAttribute"));
    }

    @Test
    public void testIgnoreAttributeReleaseToolFilter() {
        when(this.registeredService.isIgnoreAttributes()).thenReturn(true);

        final Map<String, Object> attrs = this.filter.filter("test", this.givenAttributesMap, this.registeredService);
        assertEquals(attrs.size(), 7);
    }

    @Test
    public void testPatternFilter() {
        when(this.registeredService.isIgnoreAttributes()).thenReturn(false);

        final Map<String, Object> attrs = this.filter.filter("test", this.givenAttributesMap, this.registeredService);
        assertEquals(attrs.size(), 7);

        assertFalse(attrs.containsKey("phone"));
        assertFalse(attrs.containsKey("givenName"));

        assertTrue(attrs.containsKey("uid"));
        assertTrue(attrs.containsKey("memberOf"));
        assertTrue(attrs.containsKey("mapAttribute"));

        final Map<String, String> mapAttributes = (Map<String, String>) attrs.get("mapAttribute");
        assertTrue(mapAttributes.containsKey("uid"));
        assertTrue(mapAttributes.containsKey("familyName"));
        assertFalse(mapAttributes.containsKey("phone"));

        final String[] arrayAttrs = (String[]) attrs.get("memberOf");
        assertEquals(arrayAttrs.length, 2);
    }
}
