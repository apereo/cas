package org.apereo.cas.support.pac4j.web.flow;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.context.SAMLContextProvider;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;


/**
 * Unit test of {@link IgnoreServiceRedirectUrlForSamlActionTests}.
 * 
 * Tests these cases:
 * <ul>
 * <ol>A non-SAML2 client</ol>
 * <ol>A SAML2 client without SLO services</ol>
 * <ol>A SAML2 client with SLO services</ol>
 * </ul>
 * 
 * 
 * @author jkacer
 */
public class IgnoreServiceRedirectUrlForSamlActionTests {

    private static final String CLIENT_NAME_SAML_NO_SLO = "SamlClientNoSlo";
    private static final String CLIENT_NAME_SAML_WITH_SLO = "SamlClientWithSlo";
    private static final String CLIENT_NAME_NON_SAML = "AnotherClient";

    private IgnoreServiceRedirectUrlForSamlAction actionUnderTest;   


    @Test
    public void samlClientWithoutSloShouldNotRemoveService() throws Exception {
        testServiceRemovalForParticularProfile(samlProfile(CLIENT_NAME_SAML_NO_SLO), false);
    }


    @Test
    public void samlClientWithSloShouldRemoveService() throws Exception {
        testServiceRemovalForParticularProfile(samlProfile(CLIENT_NAME_SAML_WITH_SLO), true);
    }


    @Test
    public void otherClientShouldNotRemoveService() throws Exception {
        testServiceRemovalForParticularProfile(anotherProfile(), false);
    }


    protected void testServiceRemovalForParticularProfile(final CommonProfile profile, boolean shouldBeRemoved) throws Exception {
        // Prepare the input
        MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        MockServletContext servletContext = new MockServletContext();
        ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        // Simulate that the "service" is in the flow scope
        rc.getFlowScope().put(IgnoreServiceRedirectUrlForSamlAction.FLOW_ATTR_LOGOUT_REDIR_URL, "http://my-service");

        // Assure the PAC4J profile exist
        WebContext wc = WebUtils.getPac4jJ2EContext(nativeRequest, nativeResponse);
        saveMockProfile(wc, profile);

        // Run the tested action
        actionUnderTest.doExecute(rc);
        
        if (shouldBeRemoved) {
            // Check that the service value has disappeared from the flow
            assertNull("For SAML clients with SLO services defined, the 'service' parameter should have been removed.",
                    rc.getFlowScope().get(IgnoreServiceRedirectUrlForSamlAction.FLOW_ATTR_LOGOUT_REDIR_URL));
        } else {
            // Check that the service value is still in the flow
            assertNotNull("For non-SAML2 clients and SAML clients without SLO services defined, the 'service' parameter"
                    + " should have been retained.",
                    rc.getFlowScope().get(IgnoreServiceRedirectUrlForSamlAction.FLOW_ATTR_LOGOUT_REDIR_URL));
        }
    }


    private SAML2Profile samlProfile(String clientName) {
        SAML2Profile p = new SAML2Profile();
        p.setClientName(clientName);
        return p;
    }


    private CommonProfile anotherProfile() {
        CommonProfile p = new CommonProfile();
        p.setClientName(CLIENT_NAME_NON_SAML);
        return p;
    }


    private <T extends CommonProfile> void saveMockProfile(WebContext wc, T profile) {
        @SuppressWarnings("unchecked")
        final ProfileManager<T> pm = WebUtils.getPac4jProfileManager(wc);
        pm.save(true, profile, false);
    }


    @Before
    public void setUpTestedAction() {
        SAML2Client samlClientMockNoSlo = mock(SAML2Client.class);
        mockSamlClientMetadata(samlClientMockNoSlo, false);

        SAML2Client samlClientMockWithSlo = mock(SAML2Client.class);
        mockSamlClientMetadata(samlClientMockWithSlo, true);
        
        FormClient anotherClientMock = mock(FormClient.class);

        Clients clientsMock = mock(Clients.class);
        when(clientsMock.findClient(CLIENT_NAME_SAML_NO_SLO)).thenReturn(samlClientMockNoSlo);
        when(clientsMock.findClient(CLIENT_NAME_SAML_WITH_SLO)).thenReturn(samlClientMockWithSlo);
        when(clientsMock.findClient(CLIENT_NAME_NON_SAML)).thenReturn(anotherClientMock);

        actionUnderTest = new IgnoreServiceRedirectUrlForSamlAction(clientsMock);
    }


    private void mockSamlClientMetadata(SAML2Client client, boolean hasLogoutService) {
        IDPSSODescriptor idpssoDescriptor = mock(IDPSSODescriptor.class);
        if (hasLogoutService) {
            SingleLogoutService logoutService = new DummySingleLogoutService();
            when(idpssoDescriptor.getSingleLogoutServices()).thenReturn(singletonList(logoutService));
        }

        SAMLMetadataContext samlMetadataContext = mock(SAMLMetadataContext.class);
        when(samlMetadataContext.getRoleDescriptor()).thenReturn(idpssoDescriptor);

        SAMLPeerEntityContext peerEntityContext = mock(SAMLPeerEntityContext.class);
        when(peerEntityContext.getSubcontext(SAMLMetadataContext.class, true)).thenReturn(samlMetadataContext);

        SAML2MessageContext saml2MessageContext = mock(SAML2MessageContext.class);
        when(saml2MessageContext.getSubcontext(SAMLPeerEntityContext.class, true)).thenReturn(peerEntityContext);

        SAMLContextProvider samlContextProviderMock = mock(SAMLContextProvider.class);
        when(samlContextProviderMock.buildContext(any(WebContext.class))).thenReturn(saml2MessageContext);

        when(client.getContextProvider()).thenReturn(samlContextProviderMock);
    }

}
