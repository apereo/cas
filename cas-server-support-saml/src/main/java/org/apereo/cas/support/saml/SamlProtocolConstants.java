package org.apereo.cas.support.saml;

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

    /** Parameter representing entity id. */
    String PARAMETER_ENTITY_ID = "entityId";


}
