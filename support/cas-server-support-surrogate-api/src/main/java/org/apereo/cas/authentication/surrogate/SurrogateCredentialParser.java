package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.MutableCredential;

import java.util.Optional;

/**
 * This is {@link SurrogateCredentialParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface SurrogateCredentialParser {

    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "surrogateCredentialParser";

    /**
     * Parse surrogate authentication request.
     *
     * @param credential the credential
     * @return the surrogate authentication request
     */
    Optional<SurrogateAuthenticationRequest> parse(MutableCredential credential);
}
