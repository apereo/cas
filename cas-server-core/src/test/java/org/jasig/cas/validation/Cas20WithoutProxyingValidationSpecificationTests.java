/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.validation;

import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date: 2007-01-22 15:35:37 -0500 (Mon, 22 Jan
 * 2007) $
 * @since 3.0
 */
public class Cas20WithoutProxyingValidationSpecificationTests extends TestCase {

    private Cas20WithoutProxyingValidationSpecification validationSpecification;

    protected void setUp() throws Exception {
        this.validationSpecification = new Cas20WithoutProxyingValidationSpecification();
    }

    public void testSatisfiesSpecOfTrue() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils
            .getAssertion(true)));
    }

    public void testNotSatisfiesSpecOfTrue() {
        this.validationSpecification.setRenew(true);
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils
            .getAssertion(false)));
    }

    public void testSatisfiesSpecOfFalse() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils
            .getAssertion(false)));
    }

    public void testDoesNotSatisfiesSpecOfFalse() {
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils
            .getAssertion(false, new String[] {"test2"})));
    }

    public void testSettingRenew() {
        final Cas20WithoutProxyingValidationSpecification validation = new Cas20WithoutProxyingValidationSpecification(
            true);
        assertTrue(validation.isRenew());
    }
}