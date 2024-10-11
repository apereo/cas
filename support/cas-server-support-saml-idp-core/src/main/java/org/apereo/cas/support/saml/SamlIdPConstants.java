package org.apereo.cas.support.saml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Class that exposes relevant constants and parameters to
 * the SAML IdP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SamlIdPConstants {
    /**
     * Base context path for all SAML IDP related endpoints.
     */
    String BASE_ENDPOINT_IDP = "/idp";

    /**
     * The IdP error endpoint.
     */
    String ENDPOINT_IDP_ERROR = BASE_ENDPOINT_IDP + "/error";

    /**
     * The IdP metadata endpoint.
     */
    String ENDPOINT_IDP_METADATA = BASE_ENDPOINT_IDP + "/metadata";

    /**
     * Base endpoint url for saml2 requests.
     */
    String BASE_ENDPOINT_SAML2 = BASE_ENDPOINT_IDP + "/profile/SAML2";

    /**
     * Base endpoint url for saml1 requests.
     */
    String BASE_ENDPOINT_SAML1 = BASE_ENDPOINT_IDP + "/profile/SAML1";

    /**
     * The SAML2 SSO simple-sign post profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN = BASE_ENDPOINT_SAML2 + "/POST-SimpleSign/SSO";

    /**
     * The SAML2 SSO post profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_POST = BASE_ENDPOINT_SAML2 + "/POST/SSO";

    /**
     * The SAML2 SSO redirect profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_REDIRECT = BASE_ENDPOINT_SAML2 + "/Redirect/SSO";

    /**
     * The SAML2 SLO post endpoint.
     */
    String ENDPOINT_SAML2_SLO_PROFILE_POST = BASE_ENDPOINT_SAML2 + "/POST/SLO";

    /**
     * The SAML2 SLO redirect endpoint.
     */
    String ENDPOINT_SAML2_SLO_PROFILE_REDIRECT = BASE_ENDPOINT_SAML2 + "/Redirect/SLO";

    /**
     * The SAML2 IDP initiated endpoint.
     */
    String ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO = BASE_ENDPOINT_SAML2 + "/Unsolicited/SSO";

    /**
     * The SAML2 artifact resolution endpoint.
     */
    String ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY = BASE_ENDPOINT_SAML2 + "/SOAP/AttributeQuery";


    /**
     * The SAML2 artifact resolution endpoint.
     */
    String ENDPOINT_SAML1_SOAP_ARTIFACT_RESOLUTION = BASE_ENDPOINT_SAML1 + "/SOAP/ArtifactResolution";

    /**
     * The SAML2 IDP ECP endpoint.
     */
    String ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO = BASE_ENDPOINT_SAML2 + "/SOAP/ECP";

    /**
     * The SAML2 callback profile endpoint.
     */
    String ENDPOINT_SAML2_SSO_PROFILE_CALLBACK = BASE_ENDPOINT_SAML2 + "/Callback";

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
     * The signature algorithm parameter.
     */
    String SIG_ALG = "SigAlg";
    /**
     * The signature parameter.
     */
    String SIGNATURE = "Signature";

    /**
     * The samlError constant.
     */
    String REQUEST_ATTRIBUTE_ERROR = "samlError";

    /**
     * The PAOS content type.
     */
    String ECP_SOAP_PAOS_CONTENT_TYPE = "application/vnd.paos+xml";
    /**
     * The SAML2 authentication request identifier.
     */
    String AUTHN_REQUEST_ID = "srid";

    /**
     * The view rendered when SAML2 authn request is missing.
     */
    String VIEW_ID_SAML_IDP_ERROR = "saml2-idp/casSamlIdPErrorView";

    /**
     * Structure to catalog known entity attributes
     * that drive SAML2 behavior in CAS.
     */
    @RequiredArgsConstructor
    @Getter
    enum KnownEntityAttributes {
        /**
         * If present, will enable signing operations for SAML2 assertions.
         */
        SHIBBOLETH_SIGN_ASSERTIONS("http://shibboleth.net/ns/profiles/saml2/sso/browser/signAssertions"),
        /**
         * If present, will enable signing operations for SAML2 responses.
         */
        SHIBBOLETH_SIGN_RESPONSES("http://shibboleth.net/ns/profiles/saml2/sso/browser/signResponses"),
        /**
         * If present, will enable encryption operations for SAML2 assertions.
         */
        SHIBBOLETH_ENCRYPT_ASSERTIONS("http://shibboleth.net/ns/profiles/encryptAssertions");

        private final String name;
    }
}

