package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.AuthenticationResult;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link UserAuthenticationResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface UserAuthenticationResourceEntityResponseFactory {

    /**
     * Build response response entity.
     *
     * @param result  the result
     * @param request the request
     * @return the response entity
     * @throws Exception the exception
     */
    ResponseEntity<String> build(AuthenticationResult result, HttpServletRequest request) throws Exception;
}
