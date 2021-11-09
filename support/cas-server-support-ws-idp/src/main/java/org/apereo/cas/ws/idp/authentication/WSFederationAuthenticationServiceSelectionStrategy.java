package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.jooq.lambda.Unchecked;

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

    @SneakyThrows
    private static Optional<NameValuePair> getRealmAsParameter(final Service service) {
        val builder = new URIBuilder(service.getId());
        return builder.getQueryParams()
            .stream()
            .filter(p -> p.getName().equals(WSFederationConstants.WTREALM))
            .findFirst();
    }

    private static Optional<NameValuePair> getReplyAsParameter(final Service service) {
        return Optional.ofNullable(service)
            .map(Unchecked.function(svc -> {
                val builder = new URIBuilder(svc.getId());
                return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(WSFederationConstants.WREPLY))
                    .findFirst()
                    .orElse(null);
            }));
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val replyParamRes = getReplyAsParameter(service);
        if (replyParamRes.isPresent()) {
            val serviceReply = replyParamRes.get().getValue();
            LOGGER.trace("Located service id [{}] from service authentication request at [{}]", serviceReply, service.getId());
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
        LOGGER.trace("Evaluating service requested identified as [{}]", service.getId());
        val realmAsParameter = getRealmAsParameter(service);
        if (realmAsParameter.isEmpty()) {
            LOGGER.trace("Parameter [{}] is undefined in the request", WSFederationConstants.WTREALM);
            return false;
        }
        return getReplyAsParameter(service).isPresent();
    }
}
