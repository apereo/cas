/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;

/**
 * Tests for RememberMeUsernamePasswordCredentials
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RememberMeUsernamePasswordCredentialsTests extends TestCase {
    
    public void testGettersAndSetters() {
        final RememberMeUsernamePasswordCredentials c = new RememberMeUsernamePasswordCredentials();
        c.setPassword("password");
        c.setUsername("username");
        c.setRememberMe(true);
        
        assertEquals("username", c.getUsername());
        assertEquals("password", c.getPassword());
        assertTrue(c.isRememberMe());
    }
}
