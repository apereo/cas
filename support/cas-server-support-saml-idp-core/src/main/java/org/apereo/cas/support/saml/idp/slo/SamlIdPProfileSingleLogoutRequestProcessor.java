package org.apereo.cas.support.saml.idp.slo;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlIdPProfileSingleLogoutRequestProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface SamlIdPProfileSingleLogoutRequestProcessor {
    /**
     * Supports this slo request?
     *
     * @param request        the request
     * @param response       the response
     * @param logoutRequest  the logout request
     * @param messageContext the message context
     * @return true/false
     */
    default boolean supports(final HttpServletRequest request, final HttpServletResponse response,
                             final LogoutRequest logoutRequest, final MessageContext messageContext) {
        return true;
    }

    /**
     * Process.
     *
     * @param request        the request
     * @param response       the response
     * @param logoutRequest  the logout request
     * @param messageContext the message context
     * @throws Exception the exception
     */
    void receive(HttpServletRequest request, HttpServletResponse response,
                 LogoutRequest logoutRequest, MessageContext messageContext) throws Exception;

    /**
     * Restore.
     *
     * @param requestContext the request context
     * @throws Exception the exception
     */
    default void restore(final RequestContext requestContext) throws Exception {}
}
