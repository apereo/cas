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

}

