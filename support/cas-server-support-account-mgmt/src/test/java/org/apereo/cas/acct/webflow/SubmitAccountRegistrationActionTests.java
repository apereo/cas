package org.apereo.cas.acct.webflow;

import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SubmitAccountRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
@ImportAutoConfiguration({
    CasCoreTicketsAutoConfiguration.class,
    CasAccountManagementWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.account-registration.mail.from=cas@example.org",
    "cas.account-registration.sms.from=3477562310",
    "cas.account-registration.core.registration-properties.location=classpath:/custom-registration.json"
})
class SubmitAccountRegistrationActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT)
    private Action submitAccountRegistrationAction;

    @Test
    void verifySuccessOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("username", "casuser");
        context.setParameter("email", "cas@example.org");
        context.setParameter("phone", "3477465432");
        context.setRemoteAddr("127.0.0.1");
        context.setLocalAddr("127.0.0.1");
        context.setClientInfo();
        val results = submitAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }

    @Test
    void verifyFailingOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val results = submitAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, results.getId());
    }
}
