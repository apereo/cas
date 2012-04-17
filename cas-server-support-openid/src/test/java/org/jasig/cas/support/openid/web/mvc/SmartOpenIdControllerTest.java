package org.jasig.cas.support.openid.web.mvc;

import junit.framework.TestCase;
import org.openid4java.server.ServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.mockito.Mockito.mock;


/**
 * Test case of the Smart OpenId Controller.
 */
public class SmartOpenIdControllerTest extends TestCase {
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();
    private ServerManager manager;
    private ServerAssociationStore sharedAssociations;
    private final SmartOpenIdController smartOpenIdController = new SmartOpenIdController();

    @Override
    public void setUp() {
        manager = new ServerManager();
        manager.setOPEndpointUrl("https://localshot:8443/cas/login");
        manager.setEnforceRpId(false);
        smartOpenIdController.setServerManager(manager);
    }

    public void testCanHandle() {
        request.addParameter("openid.mode", "associate");
        boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter("openid.mode");
        assertEquals(true, canHandle);
    }

    public void testCannotHandle() {
        request.addParameter("openid.mode", "anythingElse");
        boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter("openid.mode");
        assertEquals(false, canHandle);
    }

    public void testGetAssociationResponse() {
        request.addParameter("openid.mode", "associate");
        request.addParameter("openid.session_type","DH-SHA1");
        request.addParameter("openid.assoc_type","HMAC-SHA1");
        request.addParameter("openid.dh_consumer_public","NzKoFMyrzFn/5iJFPdX6MVvNA/BChV1/sJdnYbupDn7ptn+cerwEzyFfWFx25KsoLSkxQCaSMmYtc1GPy/2GI1BSKSDhpdJmDBbQRa/9Gs+giV/5fHcz/mHz8sREc7RTGI+0Ka9230arwrWt0fnoaJLRKEGUsmFR71rCo4EUOew=");
        Map<String, String> assocResponse = smartOpenIdController.getAssociationResponse(request);
        assertTrue(assocResponse.containsKey("assoc_handle"));
        assertTrue(assocResponse.containsKey("expires_in"));
        assertTrue(assocResponse.containsKey("dh_server_public"));
        assertTrue(assocResponse.containsKey("enc_mac_key"));
        request.removeParameter("openid.mode");
    }
}
