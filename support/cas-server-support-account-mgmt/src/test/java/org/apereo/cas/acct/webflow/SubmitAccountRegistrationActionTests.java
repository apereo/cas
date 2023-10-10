package org.apereo.cas.acct.webflow;

import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
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
@Import({
    CasCoreTicketsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasAccountManagementWebflowConfiguration.class
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
        context.getHttpServletRequest().setRemoteAddr("127.0.0.1");
        context.getHttpServletRequest().setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
        val results = submitAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }

    @Test
    void verifyFailingOperation() throws Throwable {
        val context = MockRequestContext.create();
        val results = submitAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, results.getId());
    }
}
