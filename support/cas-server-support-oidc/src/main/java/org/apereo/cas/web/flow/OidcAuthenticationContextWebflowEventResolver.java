package org.apereo.cas.web.flow;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcAuthenticationContextWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAuthenticationContextWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        String acr = request.getParameter(OAuthConstants.ACR_VALUES);
        if (StringUtils.isBlank(acr)) {
            final URIBuilder builderContext = new URIBuilder(context.getFlowExecutionUrl());
            final Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                    .stream().filter(p -> p.getName().equals(OAuthConstants.ACR_VALUES))
                    .findFirst();
            if (parameter.isPresent()) {
                acr = parameter.get().getValue();
            }
        }
        if (StringUtils.isBlank(acr)) {
            logger.debug("No ACR provided in the authentication request");
            return null;
        }
        final Set<String> values = org.springframework.util.StringUtils.commaDelimitedListToSet(acr);
        if (values.isEmpty()) {
            logger.debug("No ACR provided in the authentication request");
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }
        final Optional<MultifactorAuthenticationProvider> provider = providerMap.values()
                .stream()
                .filter(v -> values.contains(v.getId())).findAny();

        if (provider.isPresent()) {
            return Sets.newHashSet(new Event(this, provider.get().getId()));
        }
        logger.warn("The requested authentication class cannot be satisfied by any of the MFA providers available");
        throw new AuthenticationException();
    }
}
