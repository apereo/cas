package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasSimpleSendTokenActionTests.CasSimpleMultifactorTestConfiguration.class,
    CasSimpleMultifactorAuthenticationComponentSerializationConfiguration.class,
    CasSimpleMultifactorAuthenticationConfiguration.class,
    CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration.class,
    CasSimpleMultifactorAuthenticationTicketCatalogConfiguration.class,
    CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
    MailSenderAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCookieConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class
},
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "spring.mail.testConnection=true",
        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is %s",
        "cas.authn.mfa.simple.sms.from=347746512"
    })
@EnabledIfPortOpen(port = 25000)
@EnabledIfContinuousIntegration
@Tag("Mail")
public class CasSimpleSendTokenActionTests {
    @Autowired
    @Qualifier("mfaSimpleMultifactorSendTokenAction")
    private Action mfaSimpleMultifactorSendTokenAction;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putServiceIntoFlashScope(context, RegisteredServiceTestUtils.getService());

        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("phone", List.of("123456789"), "mail", List.of("cas@example.org")));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        val event = mfaSimpleMultifactorSendTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        val theToken = event.getAttributes().getString("token");
        assertNotNull(this.ticketRegistry.getTicket(theToken));
        val token = new CasSimpleMultifactorTokenCredential(theToken);
        val result = authenticationHandler.authenticate(token);
        assertNotNull(result);
        assertNull(this.ticketRegistry.getTicket(theToken));
    }

    @TestConfiguration
    public static class CasSimpleMultifactorTestConfiguration {
        @Bean
        public SmsSender smsSender() {
            return new MockSmsSender();
        }
    }
}
