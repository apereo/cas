/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.validation.ImmutableAssertionImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AssertionImplTests extends TestCase {

    public void testGettersForChainedPrincipals() {
        final List list = new ArrayList();

        list.add(TestUtils.getAuthentication("test"));
        list.add(TestUtils.getAuthentication("test1"));
        list.add(TestUtils.getAuthentication("test2"));

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);

        assertEquals(list.toArray(new Authentication[0]).length, assertion
            .getChainedAuthentications().length);
    }

    public void testGetterFalseForNewLogin() {
        final List list = new ArrayList();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), false);

        assertFalse(assertion.isFromNewLogin());
    }

    public void testGetterTrueForNewLogin() {
        final List list = new ArrayList();

        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);

        assertTrue(assertion.isFromNewLogin());
    }

    public void testEqualsWithNull() {
        final List list = new ArrayList();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);

        assertFalse(assertion.equals(null));
    }

    public void testEqualsWithInvalidObject() {
        final List list = new ArrayList();
        list.add(TestUtils.getAuthentication());

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);

        assertFalse(assertion.equals("test"));
    }

    public void testEqualsWithValidObject() {
        final List list = new ArrayList();
        final List list1 = new ArrayList();

        final Authentication auth = TestUtils.getAuthentication();
        list.add(auth);
        list1.add(auth);

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);
        final ImmutableAssertionImpl assertion1 = new ImmutableAssertionImpl(
            list1, new SimpleService("test"), true);

        assertTrue(assertion.equals(assertion1));
    }

    public void testGetService() {
        final Service service = new SimpleService("test");

        final List list = new ArrayList();
        list.add(new SimplePrincipal("test"));

        final Assertion assertion = new ImmutableAssertionImpl(list, service,
            false);

        assertEquals(service, assertion.getService());
    }

    public void testToString() {
        final List list = new ArrayList();

        list.add(new SimplePrincipal("test"));

        final ImmutableAssertionImpl assertion = new ImmutableAssertionImpl(
            list, new SimpleService("test"), true);
        assertEquals(ToStringBuilder.reflectionToString(assertion), assertion
            .toString());
    }

}