package org.apereo.cas.oidc.web.controllers;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;

import lombok.val;
import org.pac4j.jee.context.JEEContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link BaseOidcController}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BaseOidcController extends BaseOAuth20Controller<OidcConfigurationContext> {
    protected BaseOidcController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    protected boolean isIssuerValidForEndpoint(final HttpServletRequest request, final HttpServletResponse response, final List<String> endpoints) {
        val webContext = new JEEContext(request, response);
        return getConfigurationContext().getIssuerService().validateIssuer(webContext, endpoints);
    }
}
