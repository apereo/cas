package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcCasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class OidcCasWebflowLoginContextProvider implements CasWebflowLoginContextProvider {
    private final ArgumentExtractor argumentExtractor;

    @Override
    public Optional<String> getCandidateUsername(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val service = argumentExtractor.extractService(request);
        val hint = Optional.ofNullable(service)
            .map(svc -> svc.getAttributes().getOrDefault(OidcConstants.LOGIN_HINT, List.of()))
            .orElse(List.of());
        return hint.isEmpty() ? Optional.empty() : Optional.ofNullable(hint.get(0).toString());
    }
}
