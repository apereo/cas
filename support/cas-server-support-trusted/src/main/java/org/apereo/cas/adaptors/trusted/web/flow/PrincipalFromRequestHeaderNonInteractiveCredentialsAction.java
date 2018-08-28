package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.trusted.authentication.principal.RemoteRequestPrincipalAttributesExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link PrincipalFromRequestHeaderNonInteractiveCredentialsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class PrincipalFromRequestHeaderNonInteractiveCredentialsAction extends BasePrincipalFromNonInteractiveCredentialsAction {
    private static final int DEFAULT_SIZE = 20;
    private final String remotePrincipalHeader;

    public PrincipalFromRequestHeaderNonInteractiveCredentialsAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                                     final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                                     final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                                     final PrincipalFactory principalFactory,
                                                                     final RemoteRequestPrincipalAttributesExtractor extractor,
                                                                     final String remotePrincipalHeader) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy, principalFactory, extractor);
        this.remotePrincipalHeader = remotePrincipalHeader;
    }

    @Override
    protected String getRemotePrincipalId(final HttpServletRequest request) {
        final Principal principal = request.getUserPrincipal();
        if (principal != null) {
            LOGGER.debug("Principal [{}] found in request", principal.getName());
            return principal.getName();
        }
        final String remoteUser = request.getRemoteUser();
        if (StringUtils.isNotBlank(remoteUser)) {
            LOGGER.debug("Remote user [{}] found in HttpServletRequest", remoteUser);
            return remoteUser;
        }

        if (StringUtils.isNotBlank(this.remotePrincipalHeader)) {
            final Map<String, List<String>> headers = getAllRequestHeaderValues(request);
            LOGGER.debug("Available request headers are [{}]. Locating first header value for [{}]", headers, this.remotePrincipalHeader);
            if (headers.containsKey(this.remotePrincipalHeader)) {
                final String header = headers.get(this.remotePrincipalHeader).get(0);
                LOGGER.debug("Remote user [{}] found in [{}] header", header, this.remotePrincipalHeader);
                return header;
            }
        }
        LOGGER.debug("No remote user [{}] could be found", remoteUser);
        return null;
    }

    private Map<String, List<String>> getAllRequestHeaderValues(final HttpServletRequest request) {
        final Map<String, List<String>> headers = new LinkedHashMap<>(DEFAULT_SIZE);
        final Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            final Enumeration values = request.getHeaders(name);
            if (values != null) {
                final List<String> listValues = new ArrayList<>(DEFAULT_SIZE);
                while (values.hasMoreElements()) {
                    final String value = (String) values.nextElement();
                    listValues.add(value);
                }
                headers.put(name, listValues);
            }
        }
        return headers;
    }
}
