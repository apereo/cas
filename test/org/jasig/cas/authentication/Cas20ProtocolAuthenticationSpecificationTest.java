/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.principal.SimplePrincipal;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Cas20ProtocolAuthenticationSpecificationTest extends TestCase {

        public void testRenewGettersAndSettersFalse() {
            Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification();
            
            s.setRenew(false);
            
            assertFalse(s.isRenew());
        }

        public void testRenewGettersAndSettersTrue() {
            Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification();
            
            s.setRenew(true);
            
            assertTrue(s.isRenew());
        }
        
        public void testRenewAsTrueAsConstructor() {
            Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(true);
            
            assertTrue(s.isRenew());
        }

        public void testRenewAsFalseAsConstructor() {
            Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(false);
            
            assertFalse(s.isRenew());
        }
        
        public void testSatisfiesSpecOfTrue() {
            final List list = new ArrayList();
            final Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(true);
            list.add(new SimplePrincipal("test"));
            final Assertion assertion = new AssertionImpl(list, true);
            assertTrue(s.isSatisfiedBy(assertion));
        }

        public void testNotSatisfiesSpecOfTrue() {
            final List list = new ArrayList();
            final Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(true);
            list.add(new SimplePrincipal("test"));
            final Assertion assertion = new AssertionImpl(list, false);
            assertFalse(s.isSatisfiedBy(assertion));
        }

        public void testSatisfiesSpecOfFalse() {
            final List list = new ArrayList();
            final Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(false);
            list.add(new SimplePrincipal("test"));
            final Assertion assertion = new AssertionImpl(list, true);
            assertTrue(s.isSatisfiedBy(assertion));
        }

        public void testSatisfiesSpecOfFalse2() {
            final List list = new ArrayList();
            final Cas20ProtocolAuthenticationSpecification s = new Cas20ProtocolAuthenticationSpecification(false);
            list.add(new SimplePrincipal("test"));
            final Assertion assertion = new AssertionImpl(list, false);
            assertTrue(s.isSatisfiedBy(assertion));
        }

        
}
