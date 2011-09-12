/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.mock;

import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationSpecification;

/**
 * Class to test the Runtime exception thrown when there is no default
 * constructor on a ValidationSpecification.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class MockValidationSpecification implements ValidationSpecification {

    private boolean test;

    public MockValidationSpecification(boolean test) {
        this.test = test;
    }

    public boolean isSatisfiedBy(Assertion assertion) {
        return this.test;
    }
}
