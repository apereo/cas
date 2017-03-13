package org.apereo.cas.ws.idp;

/**
 * This is {@link WSFederationConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface WSFederationConstants {
    String SECURITY_TOKEN_ATTRIBUTE = "securityToken";
    
    String HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer";
    String HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/";
    String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_02_TRUST = "http://schemas.xmlsoap.org/ws/2005/02/trust";

    String WTREALM = "wtrealm";
    String WREPLY = "wreply";
    String WREQ = "wreq";
    String WCTX = "wctx";
    String WREFRESH = "wfresh";
    String WHR = "whr";
    String WRESULT = "wresult";
    String RELAY_STATE = "RelayState";
    String SAML_RESPONSE = "SAMLResponse";
    String STATE = "state";
    String CODE = "code";
    String WA = "wa";
    String WAUTH = "wauth";

    String ENDPOINT_FEDERATION_REQUEST = "/ws/idp/federation";
    String ENDPOINT_FEDERATION_REQUEST_CALLBACK = "/ws/idp/federationcallback";

    String WSIGNOUT10 = "wsignout1.0";
    String WSIGNOUT_CLEANUP10 = "wsignoutcleanup1.0";

    String WSIGNIN10 = "wsignin1.0";
}
