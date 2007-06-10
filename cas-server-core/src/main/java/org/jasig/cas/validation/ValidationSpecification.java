/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.validation;

/**
 * An interface to impose restrictions and requirements on validations (e.g.
 * renew=true).
 * 
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface ValidationSpecification {

    /**
     * @param assertion The assertion we want to confirm is satisfied by this
     * spec.
     * @return true if it is, false otherwise.
     */
    boolean isSatisfiedBy(Assertion assertion);
}
