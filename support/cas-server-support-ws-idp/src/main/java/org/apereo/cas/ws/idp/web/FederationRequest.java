package org.apereo.cas.ws.idp.web;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link FederationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class FederationRequest {
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

    protected FederationRequest(final String wtrealm, final String wreply, final String wctx,
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
    public static FederationRequest of(final HttpServletRequest request) {
        final String wtrealm = request.getParameter("wtrealm");
        final String wreply = request.getParameter("wreply");
        final String wreq = request.getParameter("wreq");
        final String wctx = request.getParameter("wctx");
        final String wfresh = request.getParameter("wfresh");
        final String whr = request.getParameter("whr");
        final String wresult = request.getParameter("wresult");
        final String relayState = request.getParameter("RelayState");
        final String samlResponse = request.getParameter("SAMLResponse");
        final String state = request.getParameter("state");
        final String code = request.getParameter("wtrealm");
        final String wa = request.getParameter("wa");
        final String wauth = StringUtils.defaultIfBlank(request.getParameter("wauth"), "default");
        
        return new FederationRequest(wtrealm, wreply, wctx, wfresh, whr, wresult,
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
}
