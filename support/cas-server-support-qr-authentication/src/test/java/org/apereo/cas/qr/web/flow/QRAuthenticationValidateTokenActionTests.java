package org.apereo.cas.qr.web.flow;

import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.authentication.QRAuthenticationTokenCredential;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.token.TokenConstants;
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
 * This is {@link QRAuthenticationValidateTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
class QRAuthenticationValidateTokenActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_QR_AUTHENTICATION_VALIDATE_CHANNEL)
    private Action qrAuthenticationValidateWebSocketChannelAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setParameter(TokenConstants.PARAMETER_NAME_TOKEN, "token");
        context.setParameter("deviceId", "abcdefgh987654321");
        val result = qrAuthenticationValidateWebSocketChannelAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
        assertInstanceOf(QRAuthenticationTokenCredential.class, WebUtils.getCredential(context));
    }

}
