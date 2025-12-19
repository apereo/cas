package org.apereo.cas.acct.webflow;

import module java.base;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateAccountRegistrationTokenActionTests}.
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

    "cas.account-registration.core.crypto.enabled=false",
    "cas.account-registration.mail.from=cas@example.org",
    "cas.account-registration.sms.from=3477562310",
    "cas.account-registration.core.registration-properties.location=classpath:/custom-registration.json"
})
class ValidateAccountRegistrationTokenActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN)
    private Action validateAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT)
    private Action submitAccountRegistrationAction;

    @Test
    void verifyOperationFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, validateAction.execute(context).getId());
    }

    @Test
    void verifyPassRegistrationRequest() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setRemoteAddr("127.0.0.1");
        context.setLocalAddr("127.0.0.1");
        context.setClientInfo();

        context.setParameter("username", "casuser");
        context.setParameter("email", "cas@example.org");
        context.setParameter("phone", "3477465432");

        val results = submitAccountRegistrationAction.execute(context);
        val token = new URIBuilder(results.getAttributes().get("result", String.class)).getQueryParams().getFirst().getValue();
        context.setParameter(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, token);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, validateAction.execute(context).getId());
    }
}
