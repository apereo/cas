/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: ImmutableAuthenticationTests.java,v 1.2 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public class ImmutableAuthenticationTests extends TestCase {

    private Authentication authentication;

    private Principal principal = new SimplePrincipal("test");

    private Map obj = new HashMap();

    protected void setUp() throws Exception {
        super.setUp();
        this.authentication = new ImmutableAuthentication(this.principal,
            this.obj);
    }

    public void testGetters() {
        assertEquals(this.authentication.getPrincipal(), this.principal);
        assertEquals(this.authentication.getAttributes(), this.obj);
    }
    
    public void testAuthenticatedDate() {
        assertEquals(new Date(), this.authentication.getAuthenticatedDate());
    }
    
    public void testHashCode() {
        assertEquals(HashCodeBuilder.reflectionHashCode(this.authentication), this.authentication.hashCode());
    }
    
    public void testToString() {
        assertEquals(ToStringBuilder.reflectionToString(this.authentication), this.authentication.toString());
    }
}
