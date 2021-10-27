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
package org.jasig.cas.validation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.junit.Test;

/**
 * Unit test for {@link ImmutableAssertion} class.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public class ImmutableAssertionTests {

    @Test
    public void testGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication("test"));
        list.add(TestUtils.getAuthentication("test1"));
        list.add(TestUtils.getAuthentication("test2"));

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion.getChainedAuthentications().size());
    }

    @Test
    public void testGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    @Test
    public void testGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    @Test
    public void testEqualsWithNull() {
        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertFalse(assertion.equals(null));
    }

    @Test
    public void testEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertion assertion = new ImmutableAssertion(
                TestUtils.getAuthentication(), list, TestUtils.getService(), true);

        assertFalse(assertion.equals("test"));
    }

    @Test
    public void testEqualsWithValidObject() {
        final List<Authentication> list1 = new ArrayList<Authentication>();
        final List<Authentication> list2 = new ArrayList<Authentication>();

        final Authentication auth = TestUtils.getAuthentication();
        list1.add(auth);
        list2.add(auth);

        final ImmutableAssertion assertion1 = new ImmutableAssertion(auth, list1, TestUtils.getService(), true);
        final ImmutableAssertion assertion2 = new ImmutableAssertion(auth, list2, TestUtils.getService(), true);

        assertTrue(assertion1.equals(assertion2));
    }

    @Test
    public void testGetService() {
        final Service service = TestUtils.getService();

        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertion(TestUtils.getAuthentication(), list, service, false);

        assertEquals(service, assertion.getService());
    }
}
