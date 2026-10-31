package org.apereo.cas.qr.web.flow;

import module java.base;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationGenerateCodeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.qr.json.location=file:${java.io.tmpdir}/cas-qr-devices-${random.uuid}.json",
        "CasFeatureModule.AccountManagement.enabled=true"
    })
class QRAuthenticationGenerateCodeActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_QR_AUTHENTICATION_GENERATE_CODE)
    private Action qrAuthenticationGenerateCodeAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_QR_DEVICES)
    private Action qrAuthenticationAccountProfileDevicesAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_QR_DEVICE)
    private Action qrAuthenticationAccountProfileRemoveDeviceAction;

    @Autowired
    @Qualifier("qrAuthenticationDeviceRepository")
    private QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val result = qrAuthenticationGenerateCodeAction.execute(context);
        assertNull(result);

        assertTrue(context.getFlowScope().contains("qrCode"));
        assertTrue(context.getFlowScope().contains("qrChannel"));
        assertTrue(context.getFlowScope().contains("qrPrefix"));
    }

    @Test
    void verifyAccountProfileDevices() throws Throwable {
        val principal = UUID.randomUUID().toString();
        val device = UUID.randomUUID().toString();
        val otherDevice = UUID.randomUUID().toString();
        val otherPrincipal = UUID.randomUUID().toString();
        qrAuthenticationDeviceRepository.authorizeDeviceFor(device, principal);
        qrAuthenticationDeviceRepository.authorizeDeviceFor(otherDevice, otherPrincipal);

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        assertNull(qrAuthenticationAccountProfileDevicesAction.execute(context));
        assertEquals(List.of(device), context.getFlowScope().get(QRAuthenticationAccountProfileDevicesAction.FLOW_SCOPE_ATTR_DEVICES));

        context.setParameter("deviceId", otherDevice);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, qrAuthenticationAccountProfileRemoveDeviceAction.execute(context).getId());
        assertTrue(qrAuthenticationDeviceRepository.isAuthorizedDeviceFor(otherDevice, otherPrincipal));

        context.setParameter("deviceId", device);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, qrAuthenticationAccountProfileRemoveDeviceAction.execute(context).getId());
        assertTrue(qrAuthenticationDeviceRepository.getAuthorizedDevicesFor(principal).isEmpty());
    }
}
