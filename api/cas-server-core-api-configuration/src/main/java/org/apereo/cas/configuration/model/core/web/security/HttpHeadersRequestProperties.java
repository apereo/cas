package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link HttpHeadersRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class HttpHeadersRequestProperties implements Serializable {

    private static final long serialVersionUID = 5993704062519851359L;
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

    public String getXframeOptions() {
        return xframeOptions;
    }

    public void setXframeOptions(final String xframeOptions) {
        this.xframeOptions = xframeOptions;
    }

    public String getXssOptions() {
        return xssOptions;
    }

    public void setXssOptions(final String xssOptions) {
        this.xssOptions = xssOptions;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(final boolean cache) {
        this.cache = cache;
    }

    public boolean isHsts() {
        return hsts;
    }

    public void setHsts(final boolean hsts) {
        this.hsts = hsts;
    }

    public boolean isXframe() {
        return xframe;
    }

    public void setXframe(final boolean xframe) {
        this.xframe = xframe;
    }

    public boolean isXcontent() {
        return xcontent;
    }

    public void setXcontent(final boolean xcontent) {
        this.xcontent = xcontent;
    }

    public boolean isXss() {
        return xss;
    }

    public void setXss(final boolean xss) {
        this.xss = xss;
    }

    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public void setContentSecurityPolicy(final String contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

}
