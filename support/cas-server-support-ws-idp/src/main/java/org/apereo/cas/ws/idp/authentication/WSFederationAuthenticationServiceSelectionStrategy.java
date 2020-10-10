package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.util.Optional;

/**
 * This is {@link WSFederationAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
public class WSFederationAuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = 8035218407906419228L;

    public WSFederationAuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        super(servicesManager, webApplicationServiceFactory);
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val replyParamRes = getReplyAsParameter(service);
        if (replyParamRes.isPresent()) {
            val serviceReply = replyParamRes.get().getValue();
            LOGGER.debug("Located service id [{}] from service authentication request at [{}]", serviceReply, service.getId());
            return createService(serviceReply, service);
        }
        LOGGER.trace("Resolved final service as [{}]", service);
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        if (service == null) {
            LOGGER.trace("Provided service is undefined");
            return false;
        }
        LOGGER.debug("Evaluating service requested identified as [{}]", service.getId());
        val realmAsParameter = getRealmAsParameter(service);
        if (realmAsParameter.isEmpty()) {
            LOGGER.trace("Parameter [{}] is undefined in the request", WSFederationConstants.WTREALM);
            return false;
        }
        val replyAsParameter = getReplyAsParameter(service);
        if (replyAsParameter.isEmpty()) {
            LOGGER.trace("Parameter [{}] is undefined in the request", WSFederationConstants.WREPLY);
            return false;
        }
        return true;
    }

    private static Optional<NameValuePair> getRealmAsParameter(final Service service) {
        try {
            val builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WTREALM))
                .findFirst();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    private static Optional<NameValuePair> getReplyAsParameter(final Service service) {
        try {
            if (service == null) {
                return Optional.empty();
            }

            val builder = new URIBuilder(service.getId());
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(WSFederationConstants.WREPLY))
                .findFirst();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }
}
