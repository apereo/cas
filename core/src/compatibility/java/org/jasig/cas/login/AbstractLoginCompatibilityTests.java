/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;


public abstract class AbstractLoginCompatibilityTests extends AbstractCompatibilityTests {
    public static final String FORM_USERNAME = "username";
    public static final String FORM_PASSWORD = "password";

    public AbstractLoginCompatibilityTests() throws IOException {
        super();
    }

    public AbstractLoginCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        beginAt("/login");
    }
}
