package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowMfaActions")
public class DuoSecurityAuthenticationWebflowActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("duoAuthenticationWebflowAction")
    private Action duoAuthenticationWebflowAction;

    private RequestContext context;

    @BeforeEach
    public void setup() {
        super.setup();
        context = BaseDuoSecurityTests.getMockRequestContext(applicationContext);
    }

    @Test
    public void verifyOperation() throws Exception {
        val event = duoAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

}
