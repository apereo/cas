package org.apereo.cas.support.oauth;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link OAuthCasClientRedirectActionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OAuthCasClientRedirectActionBuilder {

    /**
     * Build redirect action for the client dynamically
     * and configure the CAS client accordingly based
     * on the properties of the web context.
     *
     * @param casClient the cas client
     * @param context   the context
     * @return the redirect action
     */
    RedirectAction build(CasClient casClient, WebContext context);
}
