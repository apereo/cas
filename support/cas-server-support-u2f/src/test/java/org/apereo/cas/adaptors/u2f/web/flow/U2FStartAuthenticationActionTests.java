package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FStartAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseU2FWebflowActionTests.SharedTestConfiguration.class)
@Tag("WebflowMfaActions")
public class U2FStartAuthenticationActionTests extends BaseU2FWebflowActionTests {
    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val response = new MockHttpServletResponse();
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(id), context);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, u2fMultifactorAuthenticationProvider);
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        
        val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
        val reg1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
        val record = U2FDeviceRegistration.builder()
            .record(deviceRepository.getCipherExecutor().encode(reg1.toJsonWithAttestationCert()))
            .username(id)
            .build();
        deviceRepository.registerDevice(record);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, u2fStartAuthenticationAction.execute(context).getId());
        assertNotNull(context.getFlowScope().get("u2fAuth"));
    }
}
