package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.BaseAccepttoMultifactorAuthenticationTests;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccepttoQRCodeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.apiUrl=http://localhost:5001",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
@Tag("Webflow")
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
