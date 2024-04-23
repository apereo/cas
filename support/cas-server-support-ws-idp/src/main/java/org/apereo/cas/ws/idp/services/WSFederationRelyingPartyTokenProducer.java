package org.apereo.cas.ws.idp.services;

import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.ws.idp.web.WSFederationRequest;

import org.apache.cxf.ws.security.tokenstore.SecurityToken;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link WSFederationRelyingPartyTokenProducer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface WSFederationRelyingPartyTokenProducer {

    /**
     * Produce token for relying party.
     *
     * @param securityToken  the security token
     * @param service        the service
     * @param fedRequest     the fed request
     * @param servletRequest the servlet request
     * @param assertion      the assertion
     * @return the element; token issues for the relying party
     * @throws Exception the exception
     */
    String produce(SecurityToken securityToken, WSFederationRegisteredService service,
                   WSFederationRequest fedRequest, HttpServletRequest servletRequest,
                   TicketValidationResult assertion) throws Exception;
}
