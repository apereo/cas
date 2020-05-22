package org.apereo.cas.adaptors.trusted.web.flow;

import org.springframework.webflow.execution.Action;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link PrincipalFromRequestExtractorAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface PrincipalFromRequestExtractorAction extends Action {
    /**
     * Gets remote principal id.
     *
     * @param request the request
     * @return the remote principal id
     */
    String getRemotePrincipalId(HttpServletRequest request);
}
