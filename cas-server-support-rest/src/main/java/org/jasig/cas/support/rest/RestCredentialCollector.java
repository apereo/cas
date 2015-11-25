package org.jasig.cas.support.rest;

import org.jasig.cas.authentication.Credential;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * The {@link RestCredentialCollector} defines
 * how credentials should be extracted from the rest resource body.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface RestCredentialCollector extends Serializable {
    /**
     * Obtain credential from the request.
     *
     * @param requestBody raw entity request body
     * @return the credential instance
     */
    Credential collect(final MultiValueMap<String, String> requestBody);
}

