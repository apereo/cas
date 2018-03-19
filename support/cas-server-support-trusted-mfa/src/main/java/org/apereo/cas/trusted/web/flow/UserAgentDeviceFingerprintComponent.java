package org.apereo.cas.trusted.web.flow;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Provides the User Agent for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class UserAgentDeviceFingerprintComponent implements DeviceFingerprintComponent {
    private int order = LOWEST_PRECEDENCE;

    @Nonnull
    @Override
    public Optional<String> determineComponent(@Nonnull final String principal, @Nonnull final RequestContext context,
                                               final boolean isNew) {
        return Optional.ofNullable(WebUtils.getHttpServletRequestFromExternalWebflowContext(context))
                .map(HttpRequestUtils::getHttpServletRequestUserAgent);
    }
}
