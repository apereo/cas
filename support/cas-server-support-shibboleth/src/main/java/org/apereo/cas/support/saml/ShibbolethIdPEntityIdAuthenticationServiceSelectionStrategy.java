package org.apereo.cas.support.saml;

import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    private final int order = Ordered.HIGHEST_PRECEDENCE;
    private final transient ServiceFactory webApplicationServiceFactory;
    private final String idpServerPrefix;


    @Override
    public Service resolveServiceFrom(final Service service) {
        final var result = getEntityIdAsParameter(service);
        if (result.isPresent()) {
            final var entityId = result.get();
            LOGGER.debug("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
            return this.webApplicationServiceFactory.createService(entityId);
        }
        LOGGER.debug("Could not located entity id from service authentication request at [{}]", service.getId());
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        final var casPattern = "^".concat(idpServerPrefix).concat(".*");
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
            final var builder = new URIBuilder(service.getId());
            final var param = builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .findFirst();

            if (param.isPresent()) {
                return Optional.of(param.get().getValue());
            }
            final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            if (request != null && StringUtils.isNotBlank(request.getQueryString())) {
                final var query = request.getQueryString().split("&");
                final var paramRequest = Arrays.stream(query)
                        .map(p -> {
                            final var params = Splitter.on("=").splitToList(p);
                            return Pair.of(params.get(0), params.get(1));
                        })
                        .filter(p -> p.getKey().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                        .map(Pair::getValue)
                        .findFirst();
                return paramRequest;
            }
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
