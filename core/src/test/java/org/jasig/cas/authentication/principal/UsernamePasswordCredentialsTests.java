/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UsernamePasswordCredentialsTests extends TestCase {

    public void testSetGetUsername() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final String userName = "test";

        c.setUsername(userName);

        assertEquals(userName, c.getUsername());
    }

    public void testSetGetPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final String password = "test";

        c.setPassword(password);

        assertEquals(password, c.getPassword());
    }

    public void testToString() {
        final UsernamePasswordCredentials c = TestUtils
            .getCredentialsWithSameUsernameAndPassword();

        assertEquals(new ToStringBuilder(c).append("userName", c.getUsername())
            .toString(), c.toString());
    }

    public void testHashCOde() {
        final UsernamePasswordCredentials c = TestUtils
            .getCredentialsWithSameUsernameAndPassword();
        assertEquals(HashCodeBuilder.reflectionHashCode(c), c.hashCode());
    }
}