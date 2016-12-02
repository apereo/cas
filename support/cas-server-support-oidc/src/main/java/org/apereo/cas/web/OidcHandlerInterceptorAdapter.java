package org.apereo.cas.web;

import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This is {@link OidcHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcHandlerInterceptorAdapter extends OAuth20HandlerInterceptorAdapter {
    public OidcHandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor);
    }
}
