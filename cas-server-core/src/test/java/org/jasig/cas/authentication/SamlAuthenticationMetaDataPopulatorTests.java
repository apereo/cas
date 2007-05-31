/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.OpenIdCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.opensaml.SAMLAuthenticationStatement;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class SamlAuthenticationMetaDataPopulatorTests extends TestCase {

    private SamlAuthenticationMetaDataPopulator populator;

    protected void setUp() throws Exception {
        this.populator = new SamlAuthenticationMetaDataPopulator();
        super.setUp();
    }
    
    public void testAuthenticationTypeFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());
        
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertEquals(m2.getAttributes().get("samlAuthenticationStatement::authMethod"), SAMLAuthenticationStatement.AuthenticationMethod_Password);
    }
    
    public void testAuthenticationTypeNotFound() {
        final OpenIdCredentials credentials = new OpenIdCredentials("test", "Test");
        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());
        
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertNull(m2.getAttributes().get("samlAuthenticationStatement::authMethod"));
    }
    
    public void testAuthenticationTypeFoundCustom() {
        final OpenIdCredentials credentials = new OpenIdCredentials("test", "Test");
        
        final Map<String, String> added = new HashMap<String, String>();
        added.put("org.jasig.cas.authentication.principal.OpenIdCredentials", "FF");
        
        this.populator.setUserDefinedMappings(added);
        
        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());
        
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertEquals("FF", m2.getAttributes().get("samlAuthenticationStatement::authMethod"));
    }
}
