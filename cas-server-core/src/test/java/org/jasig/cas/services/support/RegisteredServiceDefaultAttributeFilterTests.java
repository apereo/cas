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
import java.util.List;
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
public class RegisteredServiceDefaultAttributeFilterTests {

    private RegisteredServiceAttributeFilter filter;
    private Map<String, Object> givenAttributesMap = null;

    @Mock
    private RegisteredService registeredService;

    public RegisteredServiceDefaultAttributeFilterTests() {
        this.filter = new RegisteredServiceDefaultAttributeFilter();

        this.givenAttributesMap = new HashMap<String, Object>();
        this.givenAttributesMap.put("uid", "loggedInTestUid");
        this.givenAttributesMap.put("phone", "1234567890");
        this.givenAttributesMap.put("familyName", "Smith");
        this.givenAttributesMap.put("givenName", "John");
        this.givenAttributesMap.put("employeeId", "E1234");
        this.givenAttributesMap.put("memberOf", Arrays.asList("math", "science", "chemistry"));
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(this.registeredService.getName()).thenReturn("sample test service");
        when(this.registeredService.getServiceId()).thenReturn("https://www.jasig.org");
        when(this.registeredService.getAllowedAttributes()).thenReturn(
                Arrays.asList("uid", "givenName", "memberOf", "isNotAllowed"));
    }

    @Test
    public void testDefaultFilter() {
        when(this.registeredService.isIgnoreAttributes()).thenReturn(false);
        Map<String, Object> map = this.filter.filter("uid", this.givenAttributesMap, this.registeredService);
        assertEquals(map.size(), 3);

        when(this.registeredService.isIgnoreAttributes()).thenReturn(true);
        map = this.filter.filter("uid", this.givenAttributesMap, this.registeredService);
        assertEquals(map.size(), this.givenAttributesMap.size());
        assertEquals(map, this.givenAttributesMap);

        @SuppressWarnings("unchecked")
        final List<String> memberOfAttr = (List<String>) map.get("memberOf");
        assertEquals(memberOfAttr.size(), ((List<?>) this.givenAttributesMap.get("memberOf")).size());
    }

}
