package org.jasig.cas.services.advice;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.mock.MockAuthentication;
import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

import junit.framework.TestCase;


public class CallbackServicesOnTicketGrantingTicketDestructionAfterReturningAdviceTests
    extends TestCase {
    private CallbackServicesOnTicketGrantingTicketDestructionAfterReturningAdvice advice;
    
    private MonitorServiceTicketsAfterReturningAdvice monitor;
    
    private Map singleSignoutMap;
    
    private TicketRegistry ticketRegistry;
    
    private ServiceRegistry serviceRegistry;
    
    private ServiceRegistryManager serviceRegistryManager;

    protected void setUp() throws Exception {
        this.serviceRegistry = new DefaultServiceRegistry();
        this.serviceRegistryManager = (ServiceRegistryManager) this.serviceRegistry;
        this.ticketRegistry = new DefaultTicketRegistry();
        this.singleSignoutMap = new HashMap();
        
        this.advice = new CallbackServicesOnTicketGrantingTicketDestructionAfterReturningAdvice();
        this.monitor = new MonitorServiceTicketsAfterReturningAdvice();
        
        this.advice.setServiceRegistry(this.serviceRegistry);
        this.advice.setSingleSignoutMapping(this.singleSignoutMap);
        this.advice.afterPropertiesSet();
        
        this.monitor.setSingleSignoutMapping(this.singleSignoutMap);
        this.monitor.setTicketRegistry(this.ticketRegistry);
        this.monitor.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoMap() {
        this.advice.setSingleSignoutMapping(null);
        
        try {
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testAfterPropertiesSetNoRegistry() {
        this.advice.setServiceRegistry(null);
        
        try {
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testNoTicketGrantingTicket() throws Throwable {
        this.advice.afterReturning(null, null, new Object[] {"test"}, null);
    }
    
    public void testTicketGrantingTicketExistsServiceDoesNot() throws Throwable {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new MockAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket("test2", new SimpleService("test"), new NeverExpiresExpirationPolicy());
        
        this.monitor.afterReturning(null, null, new Object[] {s}, null);
        this.advice.afterReturning(null, null, new Object[] {t.getId()}, null);
        
        assertFalse(this.singleSignoutMap.containsKey(t.getId()));
    }
    
    public void testTicketGrantingTicketAndServiceExist() throws Throwable {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new MockAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket("test2", new SimpleService("test"), new NeverExpiresExpirationPolicy());
        final RegisteredService service = new RegisteredService("test", true, true, "test", null);
        this.serviceRegistryManager.addService(service);
        
        this.monitor.afterReturning(null, null, new Object[] {s}, null);
        this.advice.afterReturning(null, null, new Object[] {t.getId()}, null);
        
        assertFalse(this.singleSignoutMap.containsKey(t.getId()));
     }
}
