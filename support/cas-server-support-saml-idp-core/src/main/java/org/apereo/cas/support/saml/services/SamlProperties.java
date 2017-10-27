package org.apereo.cas.support.saml.services;

/**
 * Interface used to group and provide SAML options for the client to choose.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
public interface SamlProperties {
    /**
     * Provides a list of metadata roles.
     */
    enum SamlMetadataRoles {
        /**
         * Marks metadata as a SP.
         */
        SPSSODescriptor,
        /**
         * Marks metadata as an IDP.
         */
        IDPSSODescriptor
    }

    /**
     * Provides a list of merge options.
     */
    enum SamlDirections {
        /**
         * Includes the metadata.
         */
        INCLUDE,
        /**
         * Excludes the metadata.
         */
        EXCLUDE
    }

    /**
     * Provides a list of default SAML name ids.
     */
    enum SamlNameIds {
        /**
         * Used to select BASIC id.
         */
        BASIC,
        /**
         * Used to select URI id.
         */
        URI,
        /**
         * Used to select Unspecified id.
         */
        UNSPECIFIED
    }

    /**
     * Provides a list of defualt credential types.
     */
    enum SamlCredentialType {
        /**
         * Used to select BASIC credentials.
         */
        BASIC,
        /**
         * Used to select X509 credentials.
         */
        X509
    }

}
