package org.apereo.cas.qr.web.flow;

import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
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
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
class QRAuthenticationGenerateCodeActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_QR_AUTHENTICATION_GENERATE_CODE)
    private Action qrAuthenticationGenerateCodeAction;

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

}
