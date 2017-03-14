package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.ws.idp.WSFederationConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link WSFederationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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

    protected WSFederationRequest(final String wtrealm, final String wreply, final String wctx,
                                  final String wfresh, final String whr,
                                  final String wresult, final String relayState,
                                  final String samlResponse, final String state,
                                  final String code, final String wa,
                                  final String wauth, final String wreq) {
        this.wtrealm = wtrealm;
        this.wreply = wreply;
        this.wctx = wctx;
        this.wfresh = wfresh;
        this.whr = whr;
        this.wresult = wresult;
        this.relayState = relayState;
        this.wa = wa;
        this.samlResponse = samlResponse;
        this.state = state;
        this.code = code;
        this.wreq = wreq;
        this.wauth = wauth;
    }

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
        
        return new WSFederationRequest(wtrealm, wreply, wctx, wfresh, whr, wresult,
                relayState, samlResponse, state, code, wa, wauth, wreq);
    }

    public String getWreq() {
        return wreq;
    }

    public String getWauth() {
        return wauth;
    }

    public String getWtrealm() {
        return wtrealm;
    }

    public String getWreply() {
        return wreply;
    }

    public String getWctx() {
        return wctx;
    }

    public String getWfresh() {
        return wfresh;
    }

    public String getWa() {
        return wa;
    }

    public String getWhr() {
        return whr;
    }

    public String getWresult() {
        return wresult;
    }

    public String getRelayState() {
        return relayState;
    }

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getState() {
        return state;
    }

    public String getCode() {
        return code;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("wtrealm", wtrealm)
                .append("wreply", wreply)
                .append("wctx", wctx)
                .append("wfresh", wfresh)
                .append("whr", whr)
                .append("wresult", wresult)
                .append("relayState", relayState)
                .append("samlResponse", samlResponse)
                .append("state", state)
                .append("code", code)
                .append("wa", wa)
                .append("wauth", wauth)
                .append("wreq", wreq)
                .toString();
    }
}
