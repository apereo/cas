package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * Provides the User Agent for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class UserAgentDeviceFingerprintComponentManager implements DeviceFingerprintComponentManager {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal, final RequestContext context) {
        return Optional.ofNullable(WebUtils.getHttpServletRequestFromExternalWebflowContext(context))
            .map(HttpRequestUtils::getHttpServletRequestUserAgent);
    }
}
