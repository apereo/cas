package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccepttoQRCodeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
})
public class AccepttoQRCodeAuthenticationHandlerTests {
    @Test
    public void verifyOperation() throws Exception {
        val handler = new AccepttoQRCodeAuthenticationHandler(mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory());
        assertTrue(handler.supports(AccepttoEmailCredential.class));
        val credential = new AccepttoEmailCredential("cas@example.org");
        assertTrue(handler.supports(credential));
        assertNotNull(handler.authenticate(credential));
    }
}
