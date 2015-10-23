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
package org.jasig.cas.services.support;

import org.apache.commons.lang3.SerializationUtils;
import org.jasig.cas.services.AttributeFilter;
import org.jasig.cas.services.RegisteredService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class RegisteredServiceRegexAttributeFilterTests {

    private final AttributeFilter filter;
    private final Map<String, Object> givenAttributesMap;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceRegexAttributeFilterTests() {

        this.filter = new RegisteredServiceRegexAttributeFilter("^.{5,}$");

        this.givenAttributesMap = new HashMap<>();
        this.givenAttributesMap.put("uid", "loggedInTestUid");
        this.givenAttributesMap.put("phone", "1290");
        this.givenAttributesMap.put("familyName", "Smith");
        this.givenAttributesMap.put("givenName", "John");
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry"));
        this.givenAttributesMap.put("arrayAttribute", new String[] {"math", "science", "chemistry"});
        this.givenAttributesMap.put("setAttribute", new HashSet<String>(Arrays.asList("math", "science", "chemistry")));

        final Map<String, String> mapAttributes = new HashMap<>();
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
    }

    @Test
    public void verifyPatternFilter() {

        final Map<String, Object> attrs = this.filter.filter(this.givenAttributesMap);
        assertEquals(attrs.size(), 7);

        assertFalse(attrs.containsKey("phone"));
        assertFalse(attrs.containsKey("givenName"));

        assertTrue(attrs.containsKey("uid"));
        assertTrue(attrs.containsKey("memberOf"));
        assertTrue(attrs.containsKey("mapAttribute"));

        @SuppressWarnings("unchecked")
        final Map<String, String> mapAttributes = (Map<String, String>) attrs.get("mapAttribute");
        assertTrue(mapAttributes.containsKey("uid"));
        assertTrue(mapAttributes.containsKey("familyName"));
        assertFalse(mapAttributes.containsKey("phone"));

        final List<?> obj = (List<?>) attrs.get("memberOf");
        assertEquals(2, obj.size());
    }
    
    @Test
    public void verifySerialization() {
        final byte[] data = SerializationUtils.serialize(this.filter);
        final AttributeFilter secondFilter = SerializationUtils.deserialize(data);
        assertEquals(secondFilter, this.filter);
    }
}
