package org.apereo.cas.web.flow;

import org.apereo.cas.adaptors.generic.remote.RemoteAuthenticationCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.model.support.generic.RemoteAuthenticationProperties;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.webflow.execution.RequestContext;
import java.util.stream.Stream;

/**
 * A webflow action that attempts to grab the remote address/cookie from the request,
 * and construct a {@link RemoteAuthenticationCredential} object.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Slf4j
public class RemoteAuthenticationNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {
    private final RemoteAuthenticationProperties properties;

    public RemoteAuthenticationNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                               final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                               final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                               final RemoteAuthenticationProperties properties) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.properties = properties;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        if (request.getCookies() != null && StringUtils.isNotBlank(properties.getCookie().getCookieName())) {
            return Stream.of(request.getCookies())
                .filter(cookie -> Strings.CI.equals(cookie.getName(), properties.getCookie().getCookieName()))
                .map(cookie -> new RemoteAuthenticationCredential(null, cookie.getValue()))
                .findFirst()
                .orElse(null);
        }
        val remoteAddress = request.getRemoteAddr();
        if (StringUtils.isNotBlank(remoteAddress) && StringUtils.isNotBlank(properties.getIpAddressRange())) {
            return new RemoteAuthenticationCredential(remoteAddress);
        }
        LOGGER.trace("No remote address or cookie found.");
        return null;
    }
}
