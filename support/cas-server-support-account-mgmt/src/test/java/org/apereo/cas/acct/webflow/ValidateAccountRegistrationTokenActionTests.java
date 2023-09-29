package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
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
import org.apache.hc.core5.net.URIBuilder;
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
 * This is {@link ValidateAccountRegistrationTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
@Import({
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasAccountManagementWebflowConfiguration.class
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
        val context = MockRequestContext.create();
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, validateAction.execute(context).getId());
    }

    @Test
    void verifyPassRegistrationRequest() throws Throwable {
        val context = MockRequestContext.create();

        context.getHttpServletRequest().setRemoteAddr("127.0.0.1");
        context.getHttpServletRequest().setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

        context.setParameter("username", "casuser");
        context.setParameter("email", "cas@example.org");
        context.setParameter("phone", "3477465432");

        val results = submitAccountRegistrationAction.execute(context);
        val token = new URIBuilder(results.getAttributes().get("result", String.class)).getQueryParams().getFirst().getValue();
        context.setParameter(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, token);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, validateAction.execute(context).getId());
    }
}
