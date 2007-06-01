/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

import junit.framework.TestCase;


public class LdapAttributeRepositoryTests extends TestCase {
    
    private LdapAttributeRepository attributeRepository;

    protected void setUp() throws Exception {
        this.attributeRepository = new LdapAttributeRepository("uid=battags,ou=people,dc=rutgers,dc=edu", "ldap://ldap1.rutgers.edu");
    }
    
    public void testAttribute() {
        final List<Attribute> a = this.attributeRepository.getAttributes();
        
        assertTrue(!a.isEmpty());
        assertNotNull(this.attributeRepository.getAttribute("l"));
    }
}
