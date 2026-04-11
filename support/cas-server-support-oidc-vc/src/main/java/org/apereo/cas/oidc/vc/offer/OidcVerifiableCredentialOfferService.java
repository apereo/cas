package org.apereo.cas.oidc.vc.offer;

import module java.base;

/**
 * This is {@link OidcVerifiableCredentialOfferService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OidcVerifiableCredentialOfferService {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialCredentialOfferService";
    
    /**
     * Create oidc verifiable credential offer.
     *
     * @param principalId                the principal id
     * @param credentialConfigurationIds the credential configuration ids
     * @return the oidc verifiable credential offer
     */
    OidcVerifiableCredentialOffer create(String principalId, List<String> credentialConfigurationIds);

    /**
     * Consume oidc verifiable credential offer.
     *
     * @param transactionId the transaction id
     * @return the oidc verifiable credential offer
     */
    OidcVerifiableCredentialOffer fetch(String transactionId);
}
