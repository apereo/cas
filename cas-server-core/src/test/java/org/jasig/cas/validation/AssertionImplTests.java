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

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.validation.ImmutableAssertionImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AssertionImplTests extends TestCase {

    public void testGettersForChainedPrincipals() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication("test"));
        list.add(TestUtils.getAuthentication("test1"));
        list.add(TestUtils.getAuthentication("test2"));

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion
            .getChainedAuthentications().size());
    }

    public void testGetterFalseForNewLogin() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), false);

        assertFalse(assertion.isFromNewLogin());
    }

    public void testGetterTrueForNewLogin() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), true);

        assertTrue(assertion.isFromNewLogin());
    }

    public void testEqualsWithNull() {
        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), true);

        assertFalse(assertion.equals(null));
    }

    public void testEqualsWithInvalidObject() {
        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), true);

        assertFalse(assertion.equals("test"));
    }

    public void testEqualsWithValidObject() {
        final List<Authentication> list = new ArrayList<Authentication>();
        final List<Authentication> list1 = new ArrayList<Authentication>();

        final Authentication auth = TestUtils.getAuthentication();
        list.add(auth);
        list1.add(auth);

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, TestUtils.getService(), true);
        final ImmutableAssertionImpl assertion1 = new ImmutableAssertionImpl(
            list1, TestUtils.getService(), true);

        assertTrue(assertion.equals(assertion1));
    }

    public void testGetService() {
        final Service service = TestUtils.getService();

        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        final Assertion assertion = new ImmutableAssertionImpl(list, service,
            false);

        assertEquals(service, assertion.getService());
    }
}