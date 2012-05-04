/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.validation;

import org.jasig.cas.TestUtils;

import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProtocolValidationSpecificationTests extends TestCase {

    public void testRenewGettersAndSettersFalse() {
        Cas20ProtocolValidationSpecification s = new Cas20ProtocolValidationSpecification();
        s.setRenew(false);
        assertFalse(s.isRenew());
    }

    public void testRenewGettersAndSettersTrue() {
        Cas20ProtocolValidationSpecification s = new Cas20ProtocolValidationSpecification();
        s.setRenew(true);
        assertTrue(s.isRenew());
    }

    public void testRenewAsTrueAsConstructor() {
        assertTrue(new Cas20ProtocolValidationSpecification(true).isRenew());
    }

    public void testRenewAsFalseAsConstructor() {
        assertFalse(new Cas20ProtocolValidationSpecification(false).isRenew());
    }

    public void testSatisfiesSpecOfTrue() {
        assertTrue(new Cas20ProtocolValidationSpecification(true)
            .isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    public void testNotSatisfiesSpecOfTrue() {
        assertFalse(new Cas20ProtocolValidationSpecification(true)
            .isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    public void testSatisfiesSpecOfFalse() {
        assertTrue(new Cas20ProtocolValidationSpecification(false)
            .isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    public void testSatisfiesSpecOfFalse2() {
        assertTrue(new Cas20ProtocolValidationSpecification(false)
            .isSatisfiedBy(TestUtils.getAssertion(false)));
    }
}