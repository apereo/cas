/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20WithoutProxyingValidationSpecificationTests extends TestCase {
    private Cas20WithoutProxyingValidationSpecification validationSpecification;
    

    protected void setUp() throws Exception {
        this.validationSpecification = new Cas20WithoutProxyingValidationSpecification();
    }


    public void testSatisfiesSpecOfTrue() {
        final List list = new ArrayList();
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new ImmutableAssertionImpl(list, true);
        assertTrue(this.validationSpecification.isSatisfiedBy(assertion));
    }

    public void testNotSatisfiesSpecOfTrue() {
        final List list = new ArrayList();
        this.validationSpecification.setRenew(true);
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new ImmutableAssertionImpl(list, false);
        assertFalse(this.validationSpecification.isSatisfiedBy(assertion));
    }

    public void testSatisfiesSpecOfFalse() {
        final List list = new ArrayList();
        list.add(new SimplePrincipal("test"));
        final Assertion assertion = new ImmutableAssertionImpl(list, true);
        assertTrue(this.validationSpecification.isSatisfiedBy(assertion));
    }

    public void testDoesNotSatisfiesSpecOfFalse() {
        final List list = new ArrayList();
        list.add(new SimplePrincipal("test"));
        list.add(new SimplePrincipal("test2"));
        final Assertion assertion = new ImmutableAssertionImpl(list, false);
        assertFalse(this.validationSpecification.isSatisfiedBy(assertion));
    }

}