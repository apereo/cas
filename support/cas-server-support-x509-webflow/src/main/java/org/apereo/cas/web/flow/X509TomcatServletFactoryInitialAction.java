package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link X509TomcatServletFactoryInitialAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class X509TomcatServletFactoryInitialAction extends AbstractAction {

    /**
     * Attribute to indicate the x509 login endpoint.
     */
    public static final String ATTRIBUTE_X509_CLIENT_AUTH_LOGIN_ENDPOINT_URL = "x509ClientAuthLoginEndpointUrl";

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val webflow = casProperties.getAuthn().getX509().getWebflow();
        val endpoint = UriComponentsBuilder
            .fromUriString(casProperties.getServer().getLoginUrl())
            .port(webflow.getPort())
            .build()
            .toUriString();
        requestContext.getFlowScope().put(ATTRIBUTE_X509_CLIENT_AUTH_LOGIN_ENDPOINT_URL, endpoint);
        return null;
    }

}
