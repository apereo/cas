package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.Credential;

import org.springframework.core.Ordered;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
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
    
    /**
     * 
     * @param request object containing headers to extract the X509Certificate(s) from
     * @param requestBody to optionally construct Credential from - if x509insecure is set to true
     * @return Credential instance
     */
    default List<Credential> fromRequest(HttpServletRequest request, MultiValueMap<String, String> requestBody) {
        return fromRequestBody(requestBody);
    }

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
