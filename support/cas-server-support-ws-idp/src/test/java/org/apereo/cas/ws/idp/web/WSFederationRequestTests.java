package org.apereo.cas.ws.idp.web;

import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class WSFederationRequestTests {

    @Test
    public void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val request = new MockHttpServletRequest();
        request.addParameter(WSFederationConstants.WTREALM, id);
        request.addParameter(WSFederationConstants.WREPLY, id);
        request.addParameter(WSFederationConstants.WREQ, id);
        request.addParameter(WSFederationConstants.WCTX, id);
        request.addParameter(WSFederationConstants.WREFRESH, id);
        request.addParameter(WSFederationConstants.WHR, id);
        request.addParameter(WSFederationConstants.WRESULT, id);
        request.addParameter(WSFederationConstants.RELAY_STATE, id);
        request.addParameter(WSFederationConstants.SAML_RESPONSE, id);
        request.addParameter(WSFederationConstants.STATE, id);
        request.addParameter(WSFederationConstants.CODE, id);
        request.addParameter(WSFederationConstants.WA, id);

        assertNotNull(WSFederationRequest.of(request));
    }
}
