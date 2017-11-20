package org.apereo.cas.configuration.support;

/**
 * This is {@link RestEndpointProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestEndpointProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = 2687020856160473089L;
    
    /**
     * HTTP method to use when contacting the rest endpoint.
     * Examples include {@code GET, POST}, etc.
     */
    @RequiredProperty
    private String method;
    
    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }
}
