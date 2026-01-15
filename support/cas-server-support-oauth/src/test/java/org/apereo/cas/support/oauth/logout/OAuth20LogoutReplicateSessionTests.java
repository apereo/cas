package org.apereo.cas.support.oauth.logout;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20LogoutReplicateSessionTests}.
 *
 * @author Jerome Leleu
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    OAuth20LogoutReplicateSessionTests.OAuthDistributedSessionTestConfiguration.class,
    AbstractOAuth20Tests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=cas",
        "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
        "cas.authn.oauth.session-replication.replicate-sessions=true",
        "cas.ticket.track-descendant-tickets=false"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Tag("OAuth")
@ExtendWith(CasTestExtension.class)
class OAuth20LogoutReplicateSessionTests {

    @Autowired
    @Qualifier(LogoutExecutionPlan.BEAN_NAME)
    private LogoutExecutionPlan logoutExecutionPlan;

    @Test
    void verifyThatTheOAuthSpecificLogoutPostProcessorApplies() {

        val processors = logoutExecutionPlan.getLogoutPostProcessors();
        assertEquals(1, processors.size());

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        val tgt = mock(TicketGrantingTicket.class);
        val processor = processors.iterator().next();
        processor.handle(tgt);
        verify(OAuthDistributedSessionTestConfiguration.SESSION_STORE).destroySession(any(JEEContext.class));
    }

    @TestConfiguration(value = "OAuthDistributedSessionTestConfiguration", proxyBeanMethods = false)
    static class OAuthDistributedSessionTestConfiguration {

        private static final SessionStore SESSION_STORE = mock(SessionStore.class);

        @Bean
        public SessionStore oauthDistributedSessionStore() {
            return SESSION_STORE;
        }
    }
}
