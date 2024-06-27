package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.credentials.SessionKeyCredentials;
import org.pac4j.core.logout.LogoutType;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientOidcLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(
    classes = {
        CasDelegatedAuthenticationOidcAutoConfiguration.class,
        BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
    })
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
class DelegatedClientOidcLogoutActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_OIDC_CLIENT_LOGOUT)
    private Action action;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyPostBackChannelOidcLogoutOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.POST);
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "OidcClient");

        val sessionIdx = UUID.randomUUID().toString();
        val clientCredentials = new ClientCredential(new SessionKeyCredentials(LogoutType.BACK, sessionIdx), "OidcClient");
        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString(), clientCredentials, Map.of("sid", List.of(sessionIdx)));
        ticketRegistry.addTicket(tgt);
        WebUtils.putCredential(context, clientCredentials);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNull(ticketRegistry.getTicket(tgt.getId()));
    }
}
