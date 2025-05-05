package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendPasswordResetInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
class SendPasswordResetInstructionsActionTests {

    @TestConfiguration(value = "PasswordManagementTestConfiguration", proxyBeanMethods = false)
    static class PasswordManagementTestConfiguration {
        @Bean
        public PasswordManagementService passwordChangeService() throws Throwable {
            val service = mock(PasswordManagementService.class);
            when(service.createToken(any())).thenReturn(null);
            when(service.findUsername(any())).thenReturn("casuser");
            when(service.findEmail(any())).thenReturn("casuser@example.org");
            return service;
        }
    }

    @Nested
    class DefaultTests extends BasePasswordManagementActionTests {

        @BeforeEach
        void setup() {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("123.456.789.000");
            request.addHeader(HttpHeaders.USER_AGENT, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));
            ticketRegistry.deleteAll();
        }

        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(HardTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }

        @Test
        void verifyNoPhoneOrEmail() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("username", "none");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        void verifyNoUsername() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pm.reset.mail.html=true",
        "cas.authn.pm.reset.mail.text=classpath:/password-reset.html"
    })
    class HtmlEmailTests extends BasePasswordManagementActionTests {
        @Test
        void verifyHtmlEmail() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(HardTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }
    }

    @Nested
    @SpringBootTest(classes = {
        BasePasswordManagementActionTests.SharedTestConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class
    }, properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.attribute-repository.stub.attributes.mail=cas@example.org",
        "cas.authn.attribute-repository.stub.attributes.givenName=casuser",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
        
        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
        "cas.authn.pm.forgot-username.mail.from=cas@example.org",
        "cas.authn.pm.reset.mail.from=cas@example.org",
        "cas.authn.pm.reset.security-questions-enabled=true",
        "cas.authn.pm.reset.number-of-uses=1"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NoMultifactorRegisteredDevicesTests extends BasePasswordManagementActionTests {

        @BeforeEach
        void setup() {
            val service = RegisteredServiceTestUtils.getService();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            servicesManager.save(registeredService);
        }
        
        @Test
        @Order(1)
        void verifyActionRequiresMfa() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            context.setParameter(SendPasswordResetInstructionsAction.REQUEST_PARAMETER_USERNAME, "user-without-devices");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_DENY, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        @Order(2)
        void verifyPasswordResetMfaBypass() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val provider = new TestMultifactorAuthenticationProvider();
            provider.setBypassEvaluator(new PrincipalMultifactorAuthenticationProviderBypassEvaluator(
                "groupMembership", "adopters", provider.getId(), applicationContext));
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider);
            context.setParameter(SendPasswordResetInstructionsAction.REQUEST_PARAMETER_USERNAME, "user-without-devices");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        @Order(0)
        void verifyActionMultiUse() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(SendPasswordResetInstructionsAction.REQUEST_PARAMETER_USERNAME, "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
            val tickets = ticketRegistry.getTickets();
            assertEquals(1, tickets.size());
            assertInstanceOf(MultiTimeUseOrTimeoutExpirationPolicy.class, tickets.iterator().next().getExpirationPolicy());
        }
    }

    @Nested
    @Import(PasswordManagementTestConfiguration.class)
    class WithoutTokens extends BasePasswordManagementActionTests {

        @Test
        void verifyNoLinkAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("username", "unknown");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }
}
