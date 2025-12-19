package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UniquePrincipalAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    UniquePrincipalAuthenticationPolicyTests.AuthenticationPolicyTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class
})
@Tag("AuthenticationPolicy")
@ExtendWith(CasTestExtension.class)
class UniquePrincipalAuthenticationPolicyTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("uniqueAuthenticationPolicy")
    private AuthenticationPolicy uniqueAuthenticationPolicy;
    
    @Test
    void verifyPolicyIsGoodUserNotFound() throws Throwable {
        assertTrue(uniqueAuthenticationPolicy.isSatisfiedBy(
            CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()),
            applicationContext).isSuccess());
    }

    @Test
    void verifyPolicyWithAssertion() throws Throwable {
        assertTrue(uniqueAuthenticationPolicy.isSatisfiedBy(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()),
            applicationContext, Map.of(Assertion.class.getName(), mock(Assertion.class))).isSuccess());
    }

    @Test
    void verifyPolicyFailsUserFoundOnce() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
            authentication, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        assertThrows(UniquePrincipalRequiredException.class,
            () -> uniqueAuthenticationPolicy.isSatisfiedBy(authentication, applicationContext));
    }
    

    @TestConfiguration(value = "AuthenticationPolicyTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationPolicyTestConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = AuthenticationServiceSelectionPlan.BEAN_NAME)
        public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan() {
            return mock(AuthenticationServiceSelectionPlan.class);
        }
        
        @Bean
        public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy() {
            return SingleSignOnParticipationStrategy.alwaysParticipating();
        }
    }
}
