package org.apereo.cas.ws.idp;

/**
 * This is {@link WSFederationConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface WSFederationConstants {
    /**
     * The default realm for the idp/sts.
     */
    String REALM_DEFAULT_URI = "urn:org:apereo:cas:ws:idp:realm-CAS";

    /**
     * Track the security token issued in the CAS assertion.
     */
    String SECURITY_TOKEN_ATTRIBUTE = "securityToken";
    /**
     * The name of the WSDL service.
     */
    String SECURITY_TOKEN_SERVICE = "SecurityTokenService";
    /**
     * The endpoint of the WSDL service.
     */
    String SECURITY_TOKEN_SERVICE_ENDPOINT = "TransportUT_Port";

    /**
     * SAML2 token type.
     */
    String WSS_SAML2_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";

    /**
     * WS-trust namespace.
     */
    String WST_NS_05_12 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    /**
     * Public key namespace.
     */
    String HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_PUBLICKEY = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/PublicKey";
    /**
     * Addressing namespace.
     */
    String HTTP_WWW_W3_ORG_2005_08_ADDRESSING = "http://www.w3.org/2005/08/addressing";
    /**
     * The bearer token type.
     */
    String HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer";
    /**
     * The ws-trust namespace.
     */
    String HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/";
    /**
     * The trust namespace.
     */
    String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_02_TRUST = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    /**
     * The identity namespace.
     */
    String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY = "http://schemas.xmlsoap.org/ws/2005/05/identity";

    /**
     * Realm.
     */
    String WTREALM = "wtrealm";
    /**
     * The constant WREPLY.
     */
    String WREPLY = "wreply";
    /**
     * The constant WREQ.
     */
    String WREQ = "wreq";
    /**
     * The constant WCTX.
     */
    String WCTX = "wctx";
    /**
     * The constant WREFRESH.
     */
    String WREFRESH = "wfresh";
    /**
     * The constant WHR.
     */
    String WHR = "whr";
    /**
     * The constant WRESULT.
     */
    String WRESULT = "wresult";
    /**
     * The constant RELAY_STATE.
     */
    String RELAY_STATE = "RelayState";
    /**
     * The constant SAML_RESPONSE.
     */
    String SAML_RESPONSE = "SAMLResponse";
    /**
     * The constant STATE.
     */
    String STATE = "state";
    /**
     * The constant CODE.
     */
    String CODE = "code";
    /**
     * The constant WA.
     */
    String WA = "wa";
    /**
     * The constant WAUTH.
     */
    String WAUTH = "wauth";

    /**
     * Endpoint where authn requests may be submitted.
     */
    String ENDPOINT_FEDERATION_REQUEST = "/ws/idp/federation";
    /**
     * Internal callback endpoint that issues tokens.
     */
    String ENDPOINT_FEDERATION_REQUEST_CALLBACK = "/ws/idp/federationcallback";

    /**
     * The STS parent endpoint.
     */
    String ENDPOINT_STS = "/ws/sts/";

    /**
     * The location of WSDL service.
     */
    String ENDPOINT_STS_REALM_WSDL = ENDPOINT_STS + "%s/STSServiceTransportUT?wsdl";

    /**
     * The constant WSIGNOUT10.
     */
    String WSIGNOUT10 = "wsignout1.0";
    /**
     * The constant WSIGNOUT_CLEANUP10.
     */
    String WSIGNOUT_CLEANUP10 = "wsignoutcleanup1.0";

    /**
     * The constant WSIGNIN10.
     */
    String WSIGNIN10 = "wsignin1.0";

    /**
     * Metadata endpoint.
     */
    String ENDPOINT_FEDERATION_METADATA = "/ws/idp/metadata";
}
