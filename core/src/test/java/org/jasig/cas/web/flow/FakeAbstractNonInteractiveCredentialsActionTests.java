/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class FakeAbstractNonInteractiveCredentialsActionTests extends
    AbstractCentralAuthenticationServiceTest {

    public void testNullCredentials() {
        AbstractNonInteractiveCredentialsAction action = new Test(1);
        
        assertEquals("error", action.doExecuteInternal(new MockRequestContext(), "test", "test", false, false, false).getId());
    }
    
    public void testCorrectCredentials() {
        AbstractNonInteractiveCredentialsAction action = new Test(3);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        assertEquals("success", action.doExecuteInternal(new MockRequestContext(), "test", "test", false, false, false).getId());
        
    }
    
    public void testCorrectCredentialsWithRenew() {
        AbstractNonInteractiveCredentialsAction action = new Test(3);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        assertEquals("success", action.doExecuteInternal(new MockRequestContext(), "test", "test", false, true, false).getId());
        
    }
    
    public void testBadCredentialsWithRenew() {
        AbstractNonInteractiveCredentialsAction action = new Test(2);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        assertEquals("error", action.doExecuteInternal(new MockRequestContext(), "test", "test", false, true, false).getId());
        
    }
    
    public void testBadCredentialsWithRenewAndExistingTicket() throws TicketException {
        String ticket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword("test2"));
        
        AbstractNonInteractiveCredentialsAction action = new Test(2);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        assertEquals("error", action.doExecuteInternal(new MockRequestContext(), ticket, "test", false, true, false).getId());
    }
    
    public void testGoodCredentialsWithRenewAndExistingTicket() throws TicketException {
        String ticket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        
        AbstractNonInteractiveCredentialsAction action = new Test(3);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        final MockRequestContext mock = new MockRequestContext();
        
        assertEquals("warn", action.doExecuteInternal(mock, ticket, "test", false, true, false).getId());
        assertNotNull(ContextUtils.getAttribute(mock, WebConstants.TICKET));
    }
    
    
    public void testBadCredentials() {
        AbstractNonInteractiveCredentialsAction action = new Test(2);
        action.setCentralAuthenticationService(getCentralAuthenticationService());
        
        assertEquals("error", action.doExecuteInternal(new MockRequestContext(), "test", "test", false, false, false).getId());
        
    }
    
    protected class Test extends AbstractNonInteractiveCredentialsAction {


        
        final int type; 
        
        protected Test(final int type) {
            this.type = type;
        }
        
        protected Credentials constructCredentialsFromRequest(final RequestContext context) {
            switch (this.type) {
                case 1:
                return null;
                
                case 2:
                    return TestUtils.getCredentialsWithDifferentUsernameAndPassword();
                    
                default:
                    return TestUtils.getCredentialsWithSameUsernameAndPassword();
            }            
        }
    }
}
