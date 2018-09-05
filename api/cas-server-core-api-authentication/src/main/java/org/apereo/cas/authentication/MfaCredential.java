package org.apereo.cas.authentication;

/**
 * This interface is used by credentials that are created by an Mfa provider.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public interface MfaCredential {

    /**
     * Method returns the provider id of the configured provider that created this credential.
     * @return - the provider id
     */
    String getProviderId();

    /**
     * Method sets the provider id of the configured provider that created this credential.
     * @return - the provider id
     */
    void setProviderId(String providerId);

}
