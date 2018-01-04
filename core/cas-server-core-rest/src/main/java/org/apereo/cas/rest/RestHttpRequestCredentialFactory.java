package org.apereo.cas.rest;

import org.apereo.cas.authentication.Credential;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

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
    List<Credential> fromRequestBody(Map<String, String> requestBody);

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
