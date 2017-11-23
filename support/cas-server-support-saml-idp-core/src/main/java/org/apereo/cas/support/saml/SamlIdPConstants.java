package org.apereo.cas.support.saml;

/**
 * Class that exposes relevant constants and parameters to
 * the SAML IdP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SamlIdPConstants {

    /**
     * The IdP metadata endpoint.
     */
    String ENDPOINT_IDP_METADATA = "/idp/metadata";

    /**
     * The SAML2 SSO simple-sign post profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN = "/idp/profile/SAML2/POST-SimpleSign/SSO";

    /**
     * The SAML2 SSO post profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_POST = "/idp/profile/SAML2/POST/SSO";

    /**
     * The SAML2 SSO redirect profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_REDIRECT = "/idp/profile/SAML2/Redirect/SSO";

    /**
     * The SAML2 SLO post endpoint.
     */
    String ENDPOINT_SAML2_SLO_PROFILE_POST = "/idp/profile/SAML2/POST/SLO";

    /**
     * The SAML2 SLO redirect endpoint.
     */
    String ENDPOINT_SAML2_SLO_PROFILE_REDIRECT = "/idp/profile/SAML2/Redirect/SLO";

    /**
     * The SAML2 IDP initiated endpoint.
     */
    String ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO = "/idp/profile/SAML2/Unsolicited/SSO";

    /**
     * The SAML2 artifact resolution endpoint.
     */
    String ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY = "/idp/profile/SAML2/SOAP/AttributeQuery";

    /**
     * The SAML2 attribute query endpoint.
     */
    String ENDPOINT_SAML2_SOAP_ARTIFACT_RESOLUTION = "/idp/profile/SAML2/SOAP/ArtifactResolution";

    /**
     * The SAML2 artifact resolution endpoint.
     */
    String ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION = "/idp/profile/SAML1/SOAP/ArtifactResolution";

    /**
     * The SAML2 IDP ECP endpoint.
     */
    String ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO = "/idp/profile/SAML2/SOAP/ECP";

    /**
     * The SAML2 callback profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK = "/idp/profile/SAML2/Callback";

    /**
     * The shire constant.
     */
    String SHIRE = "shire";

    /**
     * The provider id constant.
     */
    String PROVIDER_ID = "providerId";

    /**
     * The target constant.
     */
    String TARGET = "target";

    /**
     * The time constant.
     */
    String TIME = "time";

    /**
     * The samlError constant.
     */
    String REQUEST_ATTRIBUTE_ERROR = "samlError";

    /**
     * The PAOS content type.
     */
    String ECP_SOAP_PAOS_CONTENT_TYPE = "application/vnd.paos+xml";
}

