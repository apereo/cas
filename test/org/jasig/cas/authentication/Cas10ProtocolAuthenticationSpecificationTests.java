/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.principal.SimplePrincipal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class Cas10ProtocolAuthenticationSpecificationTests extends TestCase {

    public void testRenewGettersAndSettersFalse() {
        Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification();

        s.setRenew(false);

        assertFalse(s.isRenew());
    }

    public void testRenewGettersAndSettersTrue() {
        Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification();

        s.setRenew(true);

        assertTrue(s.isRenew());
    }

    public void testRenewAsTrueAsConstructor() {
        Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(true);

        assertTrue(s.isRenew());
    }

    public void testRenewAsFalseAsConstructor() {
        Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(false);

        assertFalse(s.isRenew());
    }

    public void testSatisfiesSpecOfTrue() {
        final List list = new ArrayList();
        final Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(true);
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new AssertionImpl(list, true);
        assertTrue(s.isSatisfiedBy(assertion));
    }

    public void testNotSatisfiesSpecOfTrue() {
        final List list = new ArrayList();
        final Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(true);
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new AssertionImpl(list, false);
        assertFalse(s.isSatisfiedBy(assertion));
    }

    public void testSatisfiesSpecOfFalse() {
        final List list = new ArrayList();
        final Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(false);
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new AssertionImpl(list, true);
        assertTrue(s.isSatisfiedBy(assertion));
    }

    public void testSatisfiesSpecOfFalse2() {
        final List list = new ArrayList();
        final Cas10ProtocolAuthenticationSpecification s = new Cas10ProtocolAuthenticationSpecification(false);
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new AssertionImpl(list, false);
        assertTrue(s.isSatisfiedBy(assertion));
    }

}
