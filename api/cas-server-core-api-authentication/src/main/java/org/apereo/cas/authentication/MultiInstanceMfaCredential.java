package org.apereo.cas.authentication;

/**
 * This interface is implemented by credential instances that are created by an Mfa provider
 * that is able to configure multiple instances of itself in the CAS server. It provides a
 * mechanisim to reslove the specific AuthenticatonHandler used to authenticate the instance
 * of the credential.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public interface MultiInstanceMfaCredential {

    /**
     * Method returns the provider id of the configured provider that created this credential.
     *
     * @return - the provider id
     */
    String getMultifactorProviderId();

    /**
     * Method sets the provider id of the configured provider that created this credential.
     *
     * @param multifactorProviderId - the provider id
     */
    void setMultifactorProviderId(String multifactorProviderId);

}
