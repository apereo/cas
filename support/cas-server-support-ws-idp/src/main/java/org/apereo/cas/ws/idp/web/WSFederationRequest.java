package org.apereo.cas.ws.idp.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link WSFederationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@AllArgsConstructor
public class WSFederationRequest {

    private final String wtrealm;

    private final String wreply;

    private final String wctx;

    private final String wfresh;

    private final String whr;

    private final String wresult;

    private final String relayState;

    private final String samlResponse;

    private final String state;

    private final String code;

    private final String wa;

    private final String wauth;

    private final String wreq;

    /**
     * Create federation request.
     *
     * @param request the request
     * @return the federation request
     */
    public static WSFederationRequest of(final HttpServletRequest request) {
        final String wtrealm = request.getParameter(WSFederationConstants.WTREALM);
        final String wreply = request.getParameter(WSFederationConstants.WREPLY);
        final String wreq = request.getParameter(WSFederationConstants.WREQ);
        final String wctx = request.getParameter(WSFederationConstants.WCTX);
        final String wfresh = request.getParameter(WSFederationConstants.WREFRESH);
        final String whr = request.getParameter(WSFederationConstants.WHR);
        final String wresult = request.getParameter(WSFederationConstants.WRESULT);
        final String relayState = request.getParameter(WSFederationConstants.RELAY_STATE);
        final String samlResponse = request.getParameter(WSFederationConstants.SAML_RESPONSE);
        final String state = request.getParameter(WSFederationConstants.STATE);
        final String code = request.getParameter(WSFederationConstants.CODE);
        final String wa = request.getParameter(WSFederationConstants.WA);
        final String wauth = StringUtils.defaultIfBlank(request.getParameter(WSFederationConstants.WAUTH), "default");
        return new WSFederationRequest(wtrealm, wreply, wctx, wfresh, whr, wresult, relayState, samlResponse, state, code, wa, wauth, wreq);
    }
}
