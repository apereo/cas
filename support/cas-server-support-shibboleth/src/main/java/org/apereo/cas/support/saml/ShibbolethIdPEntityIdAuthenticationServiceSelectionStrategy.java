package org.apereo.cas.support.saml;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.class);

    private int order = Ordered.HIGHEST_PRECEDENCE;
    private final ServiceFactory webApplicationServiceFactory;
    private final String idpServerPrefix;

    public ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(final ServiceFactory webApplicationServiceFactory,
                                                                       final String idpServerPrefix) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.idpServerPrefix = idpServerPrefix;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        final String entityId = getEntityIdAsParameter(service).get();
        LOGGER.debug("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
        return this.webApplicationServiceFactory.createService(entityId);
    }

    @Override
    public boolean supports(final Service service) {
        final String casPattern = "^".concat(idpServerPrefix).concat(".*");
        return service != null && service.getId().matches(casPattern)
                && getEntityIdAsParameter(service).isPresent();
    }

    /**
     * Gets entity id as parameter.
     *
     * @param service the service
     * @return the entity id as parameter
     */
    protected static Optional<String> getEntityIdAsParameter(final Service service) {
        try {
            final URIBuilder builder = new URIBuilder(service.getId());
            final Optional<NameValuePair> param = builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .findFirst();

            if (param.isPresent()) {
                return Optional.of(param.get().getValue());
            }
            final HttpServletRequest request = WebUtils.getHttpServletRequest();
            final String[] query = request.getQueryString().split("&");
            final Optional<String> paramRequest = Arrays.stream(query)
                    .map(p -> {
                        final String[] params = p.split("=");
                        return Pair.of(params[0], params[1]);
                    })
                    .filter(p -> p.getKey().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .map(Pair::getValue)
                    .findFirst();
            return paramRequest;
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
