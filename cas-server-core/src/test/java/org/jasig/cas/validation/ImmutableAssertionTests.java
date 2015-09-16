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
package org.jasig.cas.validation;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link org.jasig.cas.validation.ImmutableAssertion} class.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ImmutableAssertionTests {

    @Test
    public void verifyGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<>();

        list.add(TestUtils.getAuthentication("test"));
        list.add(TestUtils.getAuthentication("test1"));
        list.add(TestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void verifyGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void verifyGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void verifyEqualsWithNull() {
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertNotEquals(assertion, null);
    }

    @Test
    public void verifyEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertFalse("test".equals(assertion));
    }

    @Test
    public void verifyEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<>();
        final List<Authentication> list2 = new ArrayList<>();

        final Authentication auth = TestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, TestUtils.getService(), true);
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, TestUtils.getService(), true);

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void verifyGetService() {
        final Service service = TestUtils.getService();

        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(TestUtils.getAuthentication(), list, service, false);

        assertEquals(service, assertion.getService());
    }
}
