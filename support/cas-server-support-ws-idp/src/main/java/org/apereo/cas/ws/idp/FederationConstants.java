package org.apereo.cas.ws.idp;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link FederationConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface FederationConstants {
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

    String WSIGNOUT10 = "wsignout1.0";
    String WSIGNOUT_CLEANUP10 = "wsignoutcleanup1.0";
    
    String WSIGNIN10 = "wsignin1.0";
}
