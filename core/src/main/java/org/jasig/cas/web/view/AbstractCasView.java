/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.view;

import java.util.Map;

import org.jasig.cas.validation.Assertion;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Abstract class to handle retrieving the Assertion from the model.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractCasView extends AbstractView {

    protected final Assertion getAssertionFrom(final Map model) {
        return (Assertion) model.get("assertion");
    }
}
