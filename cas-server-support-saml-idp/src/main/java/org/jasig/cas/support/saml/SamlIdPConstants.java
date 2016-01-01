package org.jasig.cas.support.saml;

/**
 * Class that exposes relevant constants and parameters to
 * the SAML IdP.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public interface SamlIdPConstants {

    /** The IdP metadata endpoint. */
    String ENDPOINT_IDP_METADATA = "/idp/metadata";

    /** The RP metadata generation endpoint. */
    String ENDPOINT_GENERATE_RP_METADATA = "/idp/servicemetadatagen";

    /** The SAML2 SSO post profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_POST = "/idp/profile/SAML2/POST/SSO";

    /** The SAML2 SSO redirect profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_REDIRECT = "/idp/profile/SAML2/Redirect/SSO";

    /** The SAML2 SLO post endpoint. */
    String ENDPOINT_SAML2_SLO_PROFILE_POST = "/idp/profile/SAML2/POST/SLO";

    /** The SAML2 SSO post callback profile endpoint. */
    String ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK = "/idp/profile/SAML2/POST/SSO/Callback";
}

