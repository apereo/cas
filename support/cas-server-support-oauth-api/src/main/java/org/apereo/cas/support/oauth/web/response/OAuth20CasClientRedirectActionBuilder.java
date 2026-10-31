package org.apereo.cas.support.oauth.web.response;

import module java.base;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20CasClientRedirectActionBuilder}, which builds the CAS login URL that an authorization request
 * without an authenticated user is sent to.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface OAuth20CasClientRedirectActionBuilder {

    /**
     * Build the URL of the CAS login, with the callback URL as the service, and asking CAS to renew the credentials or to
     * act as a gateway as the request needs.
     *
     * @param request    the authorization request
     * @param serviceUrl the callback URL that CAS is to send the service ticket to
     * @return the login URL
     */
    String build(HttpServletRequest request, String serviceUrl);
}
