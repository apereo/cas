/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.SimpleService;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class SimpleServiceTest extends TestCase {

    public void testNullId() {
        try {
            final SimpleService service = new SimpleService(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("IllegalArgumentException expected.");
    }
    
    public void testProperId() {
        final String id = "test";
        SimpleService service = new SimpleService(id);
        
        assertEquals(id, service.getId());
    }
}
