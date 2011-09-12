/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.mock;

import org.springframework.context.ApplicationEvent;

public class MockApplicationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 3761968285092032567L;

    public MockApplicationEvent(Object arg0) {
        super(arg0);
    }

}
