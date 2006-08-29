/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.validation;

import org.jasig.cas.TestUtils;
import org.jasig.cas.validation.Cas10ProtocolValidationSpecification;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas10ProtocolValidationSpecificationTests extends TestCase {

    public void testRenewGettersAndSettersFalse() {
        Cas10ProtocolValidationSpecification s = new Cas10ProtocolValidationSpecification();
        s.setRenew(false);
        assertFalse(s.isRenew());
    }

    public void testRenewGettersAndSettersTrue() {
        Cas10ProtocolValidationSpecification s = new Cas10ProtocolValidationSpecification();
        s.setRenew(true);
        assertTrue(s.isRenew());
    }

    public void testRenewAsTrueAsConstructor() {
        assertTrue(new Cas10ProtocolValidationSpecification(true).isRenew());
    }

    public void testRenewAsFalseAsConstructor() {
        assertFalse(new Cas10ProtocolValidationSpecification(false).isRenew());
    }

    public void testSatisfiesSpecOfTrue() {
        assertTrue(new Cas10ProtocolValidationSpecification(true)
            .isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    public void testNotSatisfiesSpecOfTrue() {
        assertFalse(new Cas10ProtocolValidationSpecification(true)
            .isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    public void testSatisfiesSpecOfFalse() {
        assertTrue(new Cas10ProtocolValidationSpecification(false)
            .isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    public void testSatisfiesSpecOfFalse2() {
        assertTrue(new Cas10ProtocolValidationSpecification(false)
            .isSatisfiedBy(TestUtils.getAssertion(false)));
    }

}