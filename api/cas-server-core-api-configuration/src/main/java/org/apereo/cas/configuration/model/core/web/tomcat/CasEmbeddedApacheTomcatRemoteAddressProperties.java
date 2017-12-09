package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatRemoteAddressProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
public class CasEmbeddedApacheTomcatRemoteAddressProperties implements Serializable {
    private static final long serialVersionUID = -32143821503580896L;

    /**
     * Enable filter.
     */
    private boolean enabled;

    /**
     * A regular expression (using java.util.regex) that the remote client's IP address is compared to.
     * If this attribute is specified, the remote address MUST match for this request to be accepted.
     * If this attribute is not specified, all requests will be accepted UNLESS the remote address matches a deny pattern.
     */
    private String allowedClientIpAddressRegex = ".+";

    /**
     * A regular expression (using java.util.regex) that the remote client's IP address is compared to.
     * If this attribute is specified, the remote address MUST NOT match for this request to be accepted.
     * If this attribute is not specified, request acceptance is governed solely by the accept attribute.
     */
    private String deniedClientIpAddressRegex = ".+";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getAllowedClientIpAddressRegex() {
        return allowedClientIpAddressRegex;
    }

    public void setAllowedClientIpAddressRegex(final String allowedClientIpAddressRegex) {
        this.allowedClientIpAddressRegex = allowedClientIpAddressRegex;
    }

    public String getDeniedClientIpAddressRegex() {
        return deniedClientIpAddressRegex;
    }

    public void setDeniedClientIpAddressRegex(final String deniedClientIpAddressRegex) {
        this.deniedClientIpAddressRegex = deniedClientIpAddressRegex;
    }
}
