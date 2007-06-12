/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import org.jasig.cas.util.DSAPrivateKeyFactoryBean;
import org.jasig.cas.util.DSAPublicKeyFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class GoogleAccountsArgumentExtractorTests extends TestCase {
    
    private GoogleAccountsArgumentExtractor extractor;

    protected void setUp() throws Exception {
        final DSAPublicKeyFactoryBean pubKeyFactoryBean = new DSAPublicKeyFactoryBean();
        final DSAPrivateKeyFactoryBean privKeyFactoryBean = new DSAPrivateKeyFactoryBean();
        
        final ClassPathResource pubKeyResource = new ClassPathResource("DSAPublicKey01.key");
        final ClassPathResource privKeyResource = new ClassPathResource("DSAPrivateKey01.key");
        
        pubKeyFactoryBean.setLocation(pubKeyResource);
        privKeyFactoryBean.setLocation(privKeyResource);
        assertTrue(privKeyFactoryBean.getObjectType().equals(DSAPrivateKey.class));
        assertTrue(pubKeyFactoryBean.getObjectType().equals(DSAPublicKey.class));
        pubKeyFactoryBean.afterPropertiesSet();
        privKeyFactoryBean.afterPropertiesSet();
        
        this.extractor = new GoogleAccountsArgumentExtractor();
        this.extractor.setPrivateKey((DSAPrivateKey) privKeyFactoryBean.getObject());
        this.extractor.setPublicKey((DSAPublicKey) pubKeyFactoryBean.getObject());

        super.setUp();
    }
    
    public void testNoService() {
        assertNull(this.extractor.extractService(new MockHttpServletRequest()));
    }
}
