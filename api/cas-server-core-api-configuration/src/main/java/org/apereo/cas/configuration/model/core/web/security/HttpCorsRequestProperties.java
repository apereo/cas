package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HttpCorsRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class HttpCorsRequestProperties implements Serializable {

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
     * This is used in response to a pre-flight request.
     * The conditions under which a request is pre-flighted are discussed above.
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

    public HttpCorsRequestProperties() {
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
