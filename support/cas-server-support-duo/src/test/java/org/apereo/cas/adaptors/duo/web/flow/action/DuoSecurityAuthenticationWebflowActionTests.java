package org.apereo.cas.adaptors.duo.web.flow.action;

import module java.base;
import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=cGKL1OndjtknbmVOWaFmisaghiNFEKXHxgXCJEBr",
        "cas.authn.mfa.duo[0].duo-integration-key=DIZXVRQD3OMZ6XXMNFQ9",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityAuthenticationWebflowActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DUO_AUTHENTICATION_WEBFLOW)
    private Action duoAuthenticationWebflowAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = BaseDuoSecurityTests.getMockRequestContext(applicationContext);
        val event = duoAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

}
