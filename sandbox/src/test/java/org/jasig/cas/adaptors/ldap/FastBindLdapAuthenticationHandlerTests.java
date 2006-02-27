/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import javax.naming.directory.DirContext;

import org.jasig.cas.adaptors.ldap.util.AuthenticatedLdapContextSource;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import junit.framework.TestCase;


public class FastBindLdapAuthenticationHandlerTests extends TestCase {
    
    public void testSuccessfulLdapCall() throws AuthenticationException {
        FastBindLdapAuthenticationHandler handler = new FastBindLdapAuthenticationHandler();
        handler.setFilter("uid=%u,ou=Special Users,dc=rutgers,dc=edu");
        handler.setContextSource(new SuccessfulLdapContextSource());
        
        UsernamePasswordCredentials upc = new UsernamePasswordCredentials();
        upc.setUsername("Test");
        upc.setPassword("Test");
        assertTrue(handler.authenticate(upc));
    }
    
    public void testFailedLdapCall() throws AuthenticationException {
        FastBindLdapAuthenticationHandler handler = new FastBindLdapAuthenticationHandler();
        handler.setFilter("uid=%u,ou=Special Users,dc=rutgers,dc=edu");
        handler.setContextSource(new FailedLdapContextSource());
        
        UsernamePasswordCredentials upc = new UsernamePasswordCredentials();
        upc.setUsername("Test");
        upc.setPassword("Test");
        assertFalse(handler.authenticate(upc));
        
    }
    
    protected class SuccessfulLdapContextSource extends AuthenticatedLdapContextSource {

        public DirContext getReadOnlyContext() throws DataAccessException {
            return null;
        }

        public DirContext getReadWriteContext() throws DataAccessException {
            return null;
        }

    }

    protected class FailedLdapContextSource extends AuthenticatedLdapContextSource {

        public DirContext getReadOnlyContext() throws DataAccessException {
            throw new DataAccessResourceFailureException("");
        }

        public DirContext getReadWriteContext() throws DataAccessException {
            throw new DataAccessResourceFailureException("");
        }
    }
}
