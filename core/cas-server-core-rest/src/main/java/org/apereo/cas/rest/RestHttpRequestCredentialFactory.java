package org.apereo.cas.rest;

import org.apereo.cas.authentication.Credential;
import org.springframework.core.Ordered;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * Strategy interface for enabling plug-in point for constructing {@link Credential}
 * instances from HTTP request body.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@FunctionalInterface
public interface RestHttpRequestCredentialFactory extends Ordered {

    /**
     * Create new Credential instances from HTTP request body.
     *
     * @param requestBody to construct Credential from
     * @return Credential instance
     */
    List<Credential> fromRequestBody(MultiValueMap<String, String> requestBody);

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
