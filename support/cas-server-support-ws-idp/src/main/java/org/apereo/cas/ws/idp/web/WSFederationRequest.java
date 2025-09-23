package org.apereo.cas.ws.idp.web;

import org.apereo.cas.ws.idp.WSFederationConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link WSFederationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record WSFederationRequest(String wtrealm, String wreply, String wctx, String wfresh,
    String whr, String wresult, String relayState, String samlResponse, String state, String code,
    String wa, String wauth, String wreq) {

    /**
     * Create federation request.
     *
     * @param request the request
     * @return the federation request
     */
    public static WSFederationRequest of(final HttpServletRequest request) {
        val wtrealm = request.getParameter(WSFederationConstants.WTREALM);
        val wreply = request.getParameter(WSFederationConstants.WREPLY);
        val wreq = request.getParameter(WSFederationConstants.WREQ);
        val wctx = request.getParameter(WSFederationConstants.WCTX);
        val wfresh = request.getParameter(WSFederationConstants.WREFRESH);
        val whr = request.getParameter(WSFederationConstants.WHR);
        val wresult = request.getParameter(WSFederationConstants.WRESULT);
        val relayState = request.getParameter(WSFederationConstants.RELAY_STATE);
        val samlResponse = request.getParameter(WSFederationConstants.SAML_RESPONSE);
        val state = request.getParameter(WSFederationConstants.STATE);
        val code = request.getParameter(WSFederationConstants.CODE);
        val wa = request.getParameter(WSFederationConstants.WA);
        val wauth = StringUtils.defaultIfBlank(request.getParameter(WSFederationConstants.WAUTH), "default");
        return new WSFederationRequest(wtrealm, wreply, wctx, wfresh, whr, wresult,
            relayState, samlResponse, state, code, wa, wauth, wreq);
    }
}
