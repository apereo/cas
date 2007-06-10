/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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