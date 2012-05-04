/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.DirectMappingAuthenticationManagerImpl.DirectAuthenticationHandlerMappingHolder;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;

import junit.framework.TestCase;


public class DirectMappingAuthenticationManagerImplTests extends TestCase {

    private DirectMappingAuthenticationManagerImpl manager = new DirectMappingAuthenticationManagerImpl();

    protected void setUp() throws Exception {
        this.manager = new DirectMappingAuthenticationManagerImpl();
        
        final Map<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder> mappings = new HashMap<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder>();
        final List<AuthenticationMetaDataPopulator> populators = new ArrayList<AuthenticationMetaDataPopulator>();
        populators.add(new SamlAuthenticationMetaDataPopulator());
        
        this.manager.setAuthenticationMetaDataPopulators(populators);
        
        final DirectAuthenticationHandlerMappingHolder d = new DirectAuthenticationHandlerMappingHolder();
        d.setAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());
        d.setCredentialsToPrincipalResolver(new UsernamePasswordCredentialsToPrincipalResolver());
        
        mappings.put(UsernamePasswordCredentials.class, d);
        
        this.manager.setCredentialsMapping(mappings);
        super.setUp();
    }
    
    public void testAuthenticateUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("Test");
        c.setPassword("Test");
        final Authentication authentication = this.manager.authenticate(c);
        
        assertEquals(c.getUsername(), authentication.getPrincipal().getId());
    }
    
    public void testAuthenticateBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("Test");
        c.setPassword("Test2");
        try {
            this.manager.authenticate(c);
            fail();
        } catch (final BadCredentialsAuthenticationException e) {
            return;
        }
    }
    
    public void testAuthenticateHttp() throws Exception {
        
        try {
            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
            this.manager.authenticate(c);
            fail("Exception expected.");
        } catch (final IllegalArgumentException e) {
            return;
        }
    }
}
