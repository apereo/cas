package org.apereo.cas.support.openid.web.mvc;

import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test case of the Smart OpenId Controller.
 * @author Frederic Esnault
 * @since 3.0.0
 */
public class SmartOpenIdControllerTests extends AbstractOpenIdTests {

    private static final String OPENID_MODE_PARAM = "openid.mode";
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();

    @Autowired
    private SmartOpenIdController smartOpenIdController;

    @Test
    public void verifyCanHandle() {
        request.addParameter(OPENID_MODE_PARAM, "associate");
        final boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter(OPENID_MODE_PARAM);
        assertTrue(canHandle);
    }

    @Test
    public void verifyCannotHandle() {
        request.addParameter(OPENID_MODE_PARAM, "anythingElse");
        final boolean canHandle = smartOpenIdController.canHandle(request, response);
        request.removeParameter(OPENID_MODE_PARAM);
        assertFalse(canHandle);
    }

    @Test
    public void verifyGetAssociationResponse() {
        request.addParameter(OPENID_MODE_PARAM, "associate");
        request.addParameter("openid.session_type", "DH-SHA1");
        request.addParameter("openid.assoc_type", "HMAC-SHA1");
        request.addParameter("openid.dh_consumer_public",
                "NzKoFMyrzFn/5iJFPdX6MVvNA/BChV1/sJdnYbupDn7ptn+cerwEzyFfWFx25KsoLSkxQCaSMmYtc1GPy/2GI1BSKSDhpdJmDBb"
                + "QRa/9Gs+giV/5fHcz/mHz8sREc7RTGI+0Ka9230arwrWt0fnoaJLRKEGUsmFR71rCo4EUOew=");
        final Map<String, String> assocResponse = smartOpenIdController.getAssociationResponse(request);
        assertTrue(assocResponse.containsKey("assoc_handle"));
        assertTrue(assocResponse.containsKey("expires_in"));
        assertTrue(assocResponse.containsKey("dh_server_public"));
        assertTrue(assocResponse.containsKey("enc_mac_key"));
        request.removeParameter(OPENID_MODE_PARAM);
    }
}
