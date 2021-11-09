package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link OpenIdCasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 6.2.0
 */
@Deprecated(since = "6.2.0")
public class OpenIdCasWebflowLoginContextProvider implements CasWebflowLoginContextProvider {
    @Override
    public Optional<String> getCandidateUsername(final RequestContext context) {
        return Optional.ofNullable(WebUtils.getOpenIdLocalUserId(context));
    }
}
