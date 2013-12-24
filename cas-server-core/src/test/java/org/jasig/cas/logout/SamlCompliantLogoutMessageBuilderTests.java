package org.jasig.cas.logout;

import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class SamlCompliantLogoutMessageBuilderTests {
    
    private final LogoutMessageBuilder builder = new SamlCompliantLogoutMessageBuilder();
    @Test
    public void testMessageBuilding() {
        
        final SingleLogoutService service = mock(SingleLogoutService.class);
        final LogoutRequest request = new LogoutRequest("TICKET-ID", service);
        
        builder.build(request);
    }
}
