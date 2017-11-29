package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HttpWebRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class HttpWebRequestProperties implements Serializable {

    private static final long serialVersionUID = -5175966163542099866L;
    /**
     * Whether CAS should accept multi-valued parameters
     * in incoming requests. Example block would to prevent
     * requests where more than one {@code service} parameter is specified.
     */
    private boolean allowMultiValueParameters;
    /**
     * Parameters that are only allowed and accepted during posts.
     */
    private String onlyPostParams = "username,password";

    /**
     * Parameters to sanitize and cross-check in incoming requests.
     * The special value * instructs the Filter to check all parameters.
     */
    private String paramsToCheck =
            "ticket,service,renew,gateway,warn,method,target,SAMLart,"
                    + "pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token";

    /**
     * Control http request settings.
     */
    private Web web = new Web();
    /**
     * Enforce request header options and security settings.
     */
    private Header header = new Header();
    /**
     * Control CORS settings for requests.
     */
    private Cors cors = new Cors();

    public boolean isAllowMultiValueParameters() {
        return allowMultiValueParameters;
    }

    public void setAllowMultiValueParameters(final boolean allowMultiValueParameters) {
        this.allowMultiValueParameters = allowMultiValueParameters;
    }

    public String getOnlyPostParams() {
        return onlyPostParams;
    }

    public void setOnlyPostParams(final String onlyPostParams) {
        this.onlyPostParams = onlyPostParams;
    }

    public String getParamsToCheck() {
        return paramsToCheck;
    }

    public void setParamsToCheck(final String paramsToCheck) {
        this.paramsToCheck = paramsToCheck;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(final Cors cors) {
        this.cors = cors;
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(final Web web) {
        this.web = web;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(final Header header) {
        this.header = header;
    }

    public static class Web implements Serializable {
        private static final long serialVersionUID = -4711604991237695091L;
        /**
         * Control and specify the encoding for all http requests.
         */
        private String encoding = StandardCharsets.UTF_8.name();
        /**
         * Whether specified encoding should be forced for every request.
         * Whether the specified encoding is supposed to
         * override existing request and response encodings
         */
        private boolean forceEncoding = true;

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(final String encoding) {
            this.encoding = encoding;
        }

        public boolean isForceEncoding() {
            return forceEncoding;
        }

        public void setForceEncoding(final boolean forceEncoding) {
            this.forceEncoding = forceEncoding;
        }
    }

    public static class Cors implements Serializable {
        private static final long serialVersionUID = 5938828345939769185L;
        /**
         * Whether CORS should be enabled for http requests.
         */
        private boolean enabled;

        /**
         * The Access-Control-Allow-Credentials header Indicates
         * whether or not the response to the request can be exposed
         * when the credentials flag is true.  When used as part of a
         * response to a preflight request, this indicates whether
         * or not the actual request can be made using credentials.
         * Note that simple GET requests are not preflighted, and so
         * if a request is made for a resource with credentials, if this
         * header is not returned with the resource, the response is ignored
         * by the browser and not returned to web content.
         */
        private boolean allowCredentials = true;
        /**
         * The Origin header indicates the origin of the cross-site access request or preflight request.
         * The origin is a URI indicating the server from which the request initiated.
         * It does not include any path information, but only the server name.
         */
        private List<String> allowOrigins = new ArrayList<>();
        /**
         * The Access-Control-Allow-Methods header specifies the method or methods allowed when accessing the resource.
         * This is used in response to a preflight request.
         * The conditions under which a request is preflighted are discussed above.
         * Default is everything.
         */
        private List<String> allowMethods = new ArrayList<>();
        /**
         * The Access-Control-Allow-Headers header is used in response to a preflight
         * request to indicate which HTTP headers can be used when making the actual request.
         * Default is everything.
         */
        private List<String> allowHeaders = new ArrayList<>();
        /**
         * The Access-Control-Max-Age header indicates how long the results of a preflight request can be cached.
         */
        private long maxAge = 3_600;
        /**
         * The Access-Control-Expose-Headers header lets a server whitelist headers that browsers are allowed to access.
         */
        private List<String> exposedHeaders = new ArrayList<>();

        public Cors() {
            this.allowMethods.add("*");
            this.allowHeaders.add("*");
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(final boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public List<String> getAllowOrigins() {
            return allowOrigins;
        }

        public void setAllowOrigins(final List<String> allowOrigins) {
            this.allowOrigins = allowOrigins;
        }

        public List<String> getAllowMethods() {
            return allowMethods;
        }

        public void setAllowMethods(final List<String> allowMethods) {
            this.allowMethods = allowMethods;
        }

        public List<String> getAllowHeaders() {
            return allowHeaders;
        }

        public void setAllowHeaders(final List<String> allowHeaders) {
            this.allowHeaders = allowHeaders;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(final long maxAge) {
            this.maxAge = maxAge;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(final List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }
    }

    public static class Header implements Serializable {
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
         * Helps you reduce XSS risks on modern browsers by declaring what dynamic
         * resources are allowed to load via a HTTP Header.
         * Header value is made up of one or more directives.
         * Multiple directives are separated with a semicolon.
         */
        private String contentSecurityPolicy;

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

}
