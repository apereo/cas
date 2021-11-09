package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InweboAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class InweboAuthenticationHandlerTests {
    @Test
    public void verifyOperation() throws Exception {
        val inweboService = mock(InweboService.class);

        val response = new InweboDeviceNameResponse();
        response.setDeviceName("DeviceName");
        response.setResult(InweboResult.OK);

        when(inweboService.authenticateExtended(anyString(), anyString())).thenReturn(response);
        val handler = new InweboAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(),
            new InweboMultifactorAuthenticationProperties(), inweboService);
        val credential = new InweboCredential("token");
        credential.setOtp("otp");
        val result = handler.authenticate(credential);
        assertNotNull(result);
        assertNotNull(credential.getDeviceName());
        assertTrue(handler.supports(credential));
        assertTrue(handler.supports(credential.getCredentialClass()));
    }

    @Test
    public void verifyFailsOperation() {
        val inweboService = mock(InweboService.class);

        val response = new InweboDeviceNameResponse();
        response.setDeviceName("DeviceName");
        response.setResult(InweboResult.OK);

        when(inweboService.authenticateExtended(anyString(), anyString())).thenReturn(response);
        val handler = new InweboAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(),
            new InweboMultifactorAuthenticationProperties(), inweboService);
        val credential = new InweboCredential("token");
        assertThrows(FailedLoginException.class, () -> handler.authenticate(credential));
    }

}
