package org.apereo.cas.web.saml2;

import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockExternalContext;
import java.util.LinkedHashMap;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedClientAuthenticationRedirectActionSamlTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class DelegatedClientAuthenticationRedirectActionSamlTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT)
    private Action delegatedAuthenticationRedirectToClientAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyPost() throws Throwable {
        val context = getMockRequestContext();
        val sessionTicket = getTransientSessionTicket("SAML2ClientPostBinding");
        context.getFlowScope().put(TransientSessionTicket.class.getName(), sessionTicket);
        val result = delegatedAuthenticationRedirectToClientAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        val external = (MockExternalContext) context.getExternalContext();
        assertFalse(external.getExternalRedirectRequested());
        assertTrue(external.isResponseComplete());
        val response = (MockHttpServletResponse) WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        assertTrue(MediaType.parseMediaType(response.getContentType()).equalsTypeAndSubtype(MediaType.TEXT_HTML));
        assertNotNull(response.getContentAsString());
    }

    private MockRequestContext getMockRequestContext() throws Exception {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        return context;
    }

    private TransientSessionTicket getTransientSessionTicket(final String client) {
        val properties = new LinkedHashMap<String, Object>();
        properties.put(Client.class.getName(), client);

        val sessionTicket = mock(TransientSessionTicket.class);
        var service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.getProperties().put(RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        servicesManager.save(registeredService);

        when(sessionTicket.getService()).thenReturn(service);
        when(sessionTicket.getProperties()).thenReturn(properties);
        when(sessionTicket.getProperty(anyString(), any())).thenReturn(client);
        return sessionTicket;
    }
}
