package org.apereo.cas.web.saml2;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.execution.Action;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 7.1.0
 */
@Tag("Delegation")
@Slf4j
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class DelegatedClientAuthenticationActionSamlTests {
    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    private DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;
    
    @Test
    void verifySaml2LogoutResponse() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val client = identityProviders.findClient("SAML2Client", webContext).orElseThrow();

        context.withUserAgent();
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, client.getName());
        context.setMethod(HttpMethod.POST);

        val logoutResponse = getLogoutResponse();
        context.setContent(EncodingUtils.encodeBase64(logoutResponse));

        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        
        val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
        context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val event = delegatedAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_LOGOUT, event.getId());
    }

    private static String getLogoutResponse() {
        return "<samlp:LogoutResponse xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
            + "ID=\"_6c3737282f007720e736f0f4028feed8cb9b40291c\" Version=\"2.0\" "
            + "IssueInstant=\"" + ZonedDateTime.now(ZoneOffset.UTC) + "\" "
            + "Destination=\"http://callback.example.org?client_name=SAML2Client\" "
            + "InResponseTo=\"ONELOGIN_21df91a89767879fc0f7df6a1490c6000c81644d\">"
            + "  <saml:Issuer>https://cas.example.org/idp</saml:Issuer>"
            + "  <samlp:Status>"
            + "    <samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>"
            + "  </samlp:Status>"
            + "</samlp:LogoutResponse>";
    }
}
