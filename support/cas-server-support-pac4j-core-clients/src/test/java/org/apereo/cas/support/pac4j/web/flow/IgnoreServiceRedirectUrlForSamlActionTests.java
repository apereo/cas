package org.apereo.cas.support.pac4j.web.flow;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apereo.cas.util.Pac4jUtils;
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
 * Unit test of {@link IgnoreServiceRedirectUrlForSamlAction}.
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
 * 
 * @since 5.3.0
 */
public class IgnoreServiceRedirectUrlForSamlActionTests {

    private static final String CLIENT_NAME_SAML_NO_SLO = "SamlClientNoSlo";
    private static final String CLIENT_NAME_SAML_WITH_SLO = "SamlClientWithSlo";
    private static final String CLIENT_NAME_NON_SAML = "AnotherClient";

    private IgnoreServiceRedirectUrlForSamlAction actionUnderTest;   


    @Test
    public void samlClientWithoutSloShouldNotRemoveService() throws Exception {
        final Object logoutUrlFromFlow = testServiceRemovalForParticularProfile(samlProfile(CLIENT_NAME_SAML_NO_SLO));

        // Check that the service value is still in the flow
        assertNotNull("For SAML clients without SLO services defined, the 'service' parameter should have been retained.",
                logoutUrlFromFlow);
    }


    @Test
    public void samlClientWithSloShouldRemoveService() throws Exception {
        final Object logoutUrlFromFlow = testServiceRemovalForParticularProfile(samlProfile(CLIENT_NAME_SAML_WITH_SLO));

        // Check that the service value has disappeared from the flow
        assertNull("For SAML clients with SLO services defined, the 'service' parameter should have been removed.", logoutUrlFromFlow);
    }


    @Test
    public void otherClientShouldNotRemoveService() throws Exception {
        final Object logoutUrlFromFlow = testServiceRemovalForParticularProfile(anotherProfile());

        // Check that the service value is still in the flow
        assertNotNull("For non-SAML2 clients, the 'service' parameter should have been retained.", logoutUrlFromFlow);
    }


    /**
     * Executes the tested action on a mock request context. A provided PAC4J user profile is saved into the session and request using the
     * PAC4J manager.
     * 
     * @param profile
     *            The PAC4J user profile.
     * 
     * @return The value of the Logout URL (service) from the web flow after the tested action is executed.
     * 
     * @throws Exception
     *             When the action execution fails.
     */
    protected Object testServiceRemovalForParticularProfile(final CommonProfile profile) throws Exception {
        // Prepare the input
        final MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        final MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        final MockServletContext servletContext = new MockServletContext();
        final ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        final MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        // Simulate that the "service" is in the flow scope
        rc.getFlowScope().put(IgnoreServiceRedirectUrlForSamlAction.FLOW_ATTR_LOGOUT_REDIR_URL, "http://my-service");

        // Assure the PAC4J profile exist
        final WebContext wc = Pac4jUtils.getPac4jJ2EContext(nativeRequest, nativeResponse);
        saveMockProfile(wc, profile);

        // Run the tested action
        actionUnderTest.doExecute(rc);

        return rc.getFlowScope().get(IgnoreServiceRedirectUrlForSamlAction.FLOW_ATTR_LOGOUT_REDIR_URL);
    }


    private SAML2Profile samlProfile(final String clientName) {
        final SAML2Profile p = new SAML2Profile();
        p.setClientName(clientName);
        return p;
    }


    private CommonProfile anotherProfile() {
        final CommonProfile p = new CommonProfile();
        p.setClientName(CLIENT_NAME_NON_SAML);
        return p;
    }


    private <T extends CommonProfile> void saveMockProfile(final WebContext wc, final T profile) {
        @SuppressWarnings("unchecked")
        final ProfileManager<T> pm = Pac4jUtils.getPac4jProfileManager(wc);
        pm.save(true, profile, false);
    }


    @Before
    public void setUpTestedAction() {
        final SAML2Client samlClientMockNoSlo = mock(SAML2Client.class);
        mockSamlClientMetadata(samlClientMockNoSlo, false);

        final SAML2Client samlClientMockWithSlo = mock(SAML2Client.class);
        mockSamlClientMetadata(samlClientMockWithSlo, true);
        
        final FormClient anotherClientMock = mock(FormClient.class);

        final Clients clientsMock = mock(Clients.class);
        when(clientsMock.findClient(CLIENT_NAME_SAML_NO_SLO)).thenReturn(samlClientMockNoSlo);
        when(clientsMock.findClient(CLIENT_NAME_SAML_WITH_SLO)).thenReturn(samlClientMockWithSlo);
        when(clientsMock.findClient(CLIENT_NAME_NON_SAML)).thenReturn(anotherClientMock);

        actionUnderTest = new IgnoreServiceRedirectUrlForSamlAction(clientsMock);
    }


    private void mockSamlClientMetadata(final SAML2Client client, final boolean hasLogoutService) {
        final IDPSSODescriptor idpssoDescriptor = mock(IDPSSODescriptor.class);
        if (hasLogoutService) {
            final SingleLogoutService logoutService = new DummySingleLogoutService();
            when(idpssoDescriptor.getSingleLogoutServices()).thenReturn(singletonList(logoutService));
        }

        final SAMLMetadataContext samlMetadataContext = mock(SAMLMetadataContext.class);
        when(samlMetadataContext.getRoleDescriptor()).thenReturn(idpssoDescriptor);

        final SAMLPeerEntityContext peerEntityContext = mock(SAMLPeerEntityContext.class);
        when(peerEntityContext.getSubcontext(SAMLMetadataContext.class, true)).thenReturn(samlMetadataContext);

        final SAML2MessageContext saml2MessageContext = mock(SAML2MessageContext.class);
        when(saml2MessageContext.getSubcontext(SAMLPeerEntityContext.class, true)).thenReturn(peerEntityContext);

        final SAMLContextProvider samlContextProviderMock = mock(SAMLContextProvider.class);
        when(samlContextProviderMock.buildContext(any(WebContext.class))).thenReturn(saml2MessageContext);

        when(client.getContextProvider()).thenReturn(samlContextProviderMock);
    }

}
