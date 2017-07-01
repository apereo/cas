package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.Credential;
import org.springframework.util.MultiValueMap;

/**
 * Strategy interface for enabling plug-in point for constructing {@link Credential}
 * instances from HTTP request body.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@FunctionalInterface
public interface CredentialFactory {

    /**
     * Create new Credential instances from HTTP request body.
     *
     * @param requestBody to construct Credential from
     * @return Credential instance
     */
    Credential fromRequestBody(MultiValueMap<String, String> requestBody);
}
