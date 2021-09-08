package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateAccountRegistrationTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Mail")
@EnabledIfPortOpen(port = 25000)
@Import({
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasAccountManagementWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.account-registration.core.crypto.enabled=false",
    "cas.account-registration.mail.from=cas@example.org",
    "cas.account-registration.sms.from=3477562310",
    "cas.account-registration.core.registration-properties.location=classpath:/custom-registration.json"
})
public class ValidateAccountRegistrationTokenActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN)
    private Action validateAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT)
    private Action submitAccountRegistrationAction;

    @Test
    public void verifyOperationFails() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, validateAction.execute(context).getId());
    }

    @Test
    public void verifyPassRegistrationRequest() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        request.setRemoteAddr("127.0.0.1");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        request.addParameter("username", "casuser");
        request.addParameter("email", "cas@example.org");
        request.addParameter("phone", "3477465432");

        val results = submitAccountRegistrationAction.execute(context);
        val token = new URIBuilder(results.getAttributes().get("result", String.class)).getQueryParams().get(0).getValue();
        request.addParameter(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, token);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, validateAction.execute(context).getId());
    }
}
