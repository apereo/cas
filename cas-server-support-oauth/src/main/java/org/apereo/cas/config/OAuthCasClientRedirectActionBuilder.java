package org.apereo.cas.config;

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
    
    RedirectAction build(CasClient casClient, WebContext context);
}
