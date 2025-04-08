package org.apereo.cas.web.flow.actions;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.Client;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationStoreWebflowStateActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientAuthenticationStoreWebflowStateActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE)
    private Action delegatedAuthenticationStoreWebflowAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyMissingClient() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationStoreWebflowAction.execute(context));
    }

    @Test
    void verifyClient() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        val result = delegatedAuthenticationStoreWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
        val ticket = context.getFlowScope().get(TransientSessionTicket.class.getName(), TransientSessionTicket.class);
        assertNotNull(ticket);
        assertNotNull(ticket.getProperty(Client.class.getName(), String.class));
    }
}
