package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HttpHeadersRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class HttpHeadersRequestProperties implements Serializable {

    private static final long serialVersionUID = 5993704062519851359L;

    /**
     * Allow CAS to inject and enforce http security headers via an http filter
     * that are outlined here for caching, HSTS, etc.
     */
    private boolean enabled = true;

    /**
     * When true, will inject the following headers into the response for non-static resources.
     * <pre>
     * Cache-Control: no-cache, no-store, max-age=0, must-revalidate
     * Pragma: no-cache
     * Expires: 0
     * </pre>
     */
    private boolean cache = true;

    /**
     * When true, will inject the following headers into the response:
     * {@code Strict-Transport-Security: max-age=15768000 ; includeSubDomains}.
     */
    private boolean hsts = true;

    /**
     * When true, will inject the following headers into the response: {@code X-Frame-Options: DENY}.
     */
    private boolean xframe = true;

    /**
     * When true, will inject the following headers into the response: {@code X-Content-Type-Options: nosniff}.
     */
    private boolean xcontent = true;

    /**
     * When true, will inject the following headers into the response: {@code X-XSS-Protection: 1; mode=block}.
     */
    private boolean xss = true;

    /**
     * Will inject values into the {@code X-Frame-Options} header into the response.
     */
    private String xframeOptions = "DENY";

    /**
     * Will inject values into the {@code X-XSS-Protection} header into the response.
     */
    private String xssOptions = "1; mode=block";

    /**
     * Helps you reduce XSS risks on modern browsers by declaring what dynamic
     * resources are allowed to load via a HTTP Header.
     * Header value is made up of one or more directives.
     * Multiple directives are separated with a semicolon.
     */
    private String contentSecurityPolicy;
}
