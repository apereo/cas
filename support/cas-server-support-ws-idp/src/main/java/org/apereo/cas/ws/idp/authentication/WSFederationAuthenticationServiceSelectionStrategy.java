package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * This is {@link WSFederationAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class WSFederationAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = 8035218407906419228L;

    private final int order = Ordered.HIGHEST_PRECEDENCE;
    private final transient ServiceFactory webApplicationServiceFactory;

    @Override
    public Service resolveServiceFrom(final Service service) {
        if (service != null) {
            final Optional<NameValuePair> replyAsParameter = getReplyAsParameter(service);
            if (replyAsParameter.isPresent()) {
                final String serviceReply = replyAsParameter.get().getValue();
                LOGGER.debug("Located service id [{}] from service authentication request at [{}]", serviceReply, service.getId());
                return this.webApplicationServiceFactory.createService(serviceReply);
            }
        }
        LOGGER.debug("Resolved final service as [{}]", service);
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        if (service == null) {
            LOGGER.debug("Provided service is undefined");
            return false;
        }
        LOGGER.debug("Evaluating service requested identified as [{}]", service.getId());
        final Optional<NameValuePair> realmAsParameter = getRealmAsParameter(service);
        if (!realmAsParameter.isPresent()) {
            LOGGER.debug("Parameter [{}] is undefined in the request", WSFederationConstants.WTREALM);
            return false;
        }
        final Optional<NameValuePair> replyAsParameter = getReplyAsParameter(service);
        if (!replyAsParameter.isPresent()) {
            LOGGER.debug("Parameter [{}] is undefined in the request", WSFederationConstants.WREPLY);
            return false;
        }
        return true;
    }

    private static Optional<NameValuePair> getRealmAsParameter(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            final Optional param = builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WTREALM))
                .findFirst();
            return param;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static Optional<NameValuePair> getReplyAsParameter(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            final Optional param = builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WREPLY))
                .findFirst();
            return param;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
