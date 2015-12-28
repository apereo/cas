package org.jasig.cas.support.saml;

/**
 * Class that exposes relevant constants and parameters to
 * the SAML protocol. These include attribute names, pre-defined
 * values and expected request parameter names as is specified
 * by the protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface SamlProtocolConstants {
    /** Constant representing the saml request. */
    String PARAMETER_SAML_REQUEST = "SAMLRequest";

    /** Constant representing the saml response. */
    String PARAMETER_SAML_RESPONSE = "SAMLResponse";

    /** Constant representing the saml relay state. */
    String PARAMETER_SAML_RELAY_STATE = "RelayState";

    /** Constant representing artifact. */
    String CONST_PARAM_ARTIFACT = "SAMLart";

    /** Constant representing service. */
    String CONST_PARAM_TARGET = "TARGET";

    /** Indicates the endpoint for saml validation. */
    String ENDPOINT_SAML_VALIDATE = "/samlValidate";

    /** The SAML2 SSO post profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_POST = "/idp/profile/SAML2/POST/SSO";

    /** The SAML2 SSO redirect profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_REDIRECT = "/idp/profile/SAML2/Redirect/SSO";

    /** The SAML2 SSO post callback profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK = "/idp/profile/SAML2/POST/SSO/Callback";

}
