package org.jasig.cas.support.rest;

import org.jasig.cas.authentication.Credential;
import org.springframework.util.MultiValueMap;

import javax.validation.constraints.NotNull;

/**
 * Strategy interface for enabling plug-in point for constructing {@link org.jasig.cas.authentication.Credential}
 * instances from HTTP request body.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public interface CredentialFactory {

    /**
     * Create new Credential instances from HTTP request body.
     *
     * @param requestBody to construct Credential from
     * @return Credential instance
     */
    Credential fromRequestBody(@NotNull MultiValueMap<String, String> requestBody);
}
