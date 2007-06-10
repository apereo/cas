/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGeneratorTests extends TestCase {

    public void testGenerator() {
        final ShibbolethCompatiblePersistentIdGenerator generator = new ShibbolethCompatiblePersistentIdGenerator();
        generator.setSalt("scottssalt");

        final Principal p = TestUtils.getPrincipal();
        final Service s = TestUtils.getService();
        
        final String value = generator.generate(p, s);
        
        assertNotNull(value); 
    }

}
