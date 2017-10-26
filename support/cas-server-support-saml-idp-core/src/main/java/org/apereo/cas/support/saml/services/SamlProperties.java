package org.apereo.cas.support.saml.services;

public interface SamlProperties {

    enum SamlMetadataRoles {
        SPSSODescriptor,
        IDPSSODescriptor
    }

    enum SamlDirections {
        INCLUDE,
        EXCLUDE
    }

    enum SamlNameIds {
        BASIC,
        URI,
        UNSPECIFIED
    }

    enum SamlCredentialType {
        BASIC,
        X509
    }

}
