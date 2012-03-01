package org.jasig.cas;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.ticket.*;
import org.jasig.cas.ticket.registry.TicketRegistry;


import java.util.List;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests with the help of Mockito framework
 *
 * @author Dmitriy Kopylenko
 */
public class CentralAuthenticationServiceImplWithMokitoTests {

    @Test
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxy_CAS1019() throws TicketException {
        //Main class under test
        CentralAuthenticationServiceImpl cas = new CentralAuthenticationServiceImpl();

        //Mock ST
        ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getId()).thenReturn("st-id");

        //Mock TGT
        TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(false);
        when(tgtMock.grantServiceTicket(anyString(), any(Service.class), any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock);
        List<Authentication> authnListMock = mock(List.class);
        when(authnListMock.size()).thenReturn(2); // <-- criteria for testing the CAS-1019 feature
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        //Mock TicketRegistry
        TicketRegistry ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(anyString(), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);

        //Mock ServicesManager
        RegisteredServiceImpl registeredService = new RegisteredServiceImpl();
        registeredService.setAllowedToProxy(false); // <-- criteria for testing the CAS-1019 feature
        ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(any(Service.class))).thenReturn(registeredService);

        //Set the stubbed dependencies
        cas.setTicketRegistry(ticketRegMock);
        cas.setServicesManager(smMock);

        //Finally, test the feature
        try{
            cas.grantServiceTicket("tgt-id", TestUtils.getService());
            fail("Should have thrown UnauthorizedProxyingException");
        }
        catch (UnauthorizedProxyingException e) {
            //Expected
        }
    }
}
