package org.apereo.cas.web.saml2;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.logout.processor.LogoutProcessor;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationClientFinishLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DelegatedAuthenticationClientFinishLogoutActionTests {
    
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT)
    private Action delegatedAuthenticationClientFinishLogoutAction;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @Order(1)
    void verifyOperationWithRedirect() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.withUserAgent();
        DelegationWebflowUtils.putDelegatedAuthenticationClientName(context, "SAML2Client");
        WebUtils.putLogoutRedirectUrl(context, "https://google.com");

        val samlClient = (SAML2Client) identityProviders.findClient("SAML2RedirectLogoutClient").orElseThrow();
        samlClient.init();
        val userProfile = new SAML2Profile();
        userProfile.setId("casuser");

        val callContext = new CallContext(new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse()), new JEESessionStore());
        val action = (WithLocationAction) samlClient.getLogoutActionBuilder()
            .getLogoutAction(callContext, userProfile, "https://google.com")
            .orElseThrow();

        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder()
            .target("https://google.com")
            .location(action.getLocation())
            .status(302)
            .build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(context, logoutRequest);

        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequestTicket(context));
    }

    @Test
    @Order(1)
    void verifyOperationNoLogoutRedirectUrl() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.withUserAgent();
        DelegationWebflowUtils.putDelegatedAuthenticationClientName(context, "SAML2Client");
        val logoutRequest = DelegatedAuthenticationClientLogoutRequest.builder().status(200).build();
        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(context, logoutRequest);
        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    @Order(1)
    void verifyOperationWithRelay() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.withUserAgent();
        context.setParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "SAML2Client");
        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
    }

    @Test
    @Order(100)
    void verifyOperationFailsWithError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.withUserAgent();
        context.setParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, "SAML2Client");
        val samlClient = (SAML2Client) identityProviders.findClient("SAML2Client").orElseThrow();
        samlClient.init();

        val handler = mock(LogoutProcessor.class);
        when(handler.processLogout(any(), any())).thenReturn(new FoundAction("https://google.com"));
        samlClient.setLogoutProcessor(handler);

        val credentialExtractor = mock(CredentialsExtractor.class);
        when(credentialExtractor.extract(any())).thenReturn(Optional.of(mock(SAML2Credentials.class)));
        samlClient.setCredentialsExtractor(credentialExtractor);

        val result = delegatedAuthenticationClientFinishLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.FOUND.value(), context.getHttpServletResponse().getStatus());
        assertEquals("https://google.com", context.getHttpServletResponse().getHeader("Location"));
    }

}
