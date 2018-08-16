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
     * Create new Credential instances from HTTP request or requestBody.
     * 
     * @param request object to extract credentials from
     * @param requestBody multipart/form-data request body to extract credentials from
     * @return Credential instance(s)
     */
    List<Credential> fromRequest(HttpServletRequest request, MultiValueMap<String, String> requestBody);

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
