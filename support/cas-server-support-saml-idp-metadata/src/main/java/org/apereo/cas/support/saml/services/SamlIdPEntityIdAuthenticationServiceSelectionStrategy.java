package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.saml.SamlProtocolConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.util.URIBuilder;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * This is {@link SamlIdPEntityIdAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Setter
@Getter
public class SamlIdPEntityIdAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;
    private final int order = Ordered.HIGHEST_PRECEDENCE;
    private final transient ServiceFactory webApplicationServiceFactory;
    private final String casServiceUrlPattern;

    public SamlIdPEntityIdAuthenticationServiceSelectionStrategy(final ServiceFactory webApplicationServiceFactory,
                                                                 final String casServerPrefix) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.casServiceUrlPattern = "^".concat(casServerPrefix).concat(".*");
    }

    /**
     * Gets entity id as parameter.
     *
     * @param service the service
     * @return the entity id as parameter
     */
    protected static Optional<URIBuilder.BasicNameValuePair> getEntityIdAsParameter(final Service service) {
        val builder = new URIBuilder(service.getId());
        return builder.getQueryParams()
            .stream()
            .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
            .findFirst();
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val entityId = getEntityIdAsParameter(service).orElseThrow().getValue();
        LOGGER.trace("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
        return this.webApplicationServiceFactory.createService(entityId);
    }

    @Override
    public boolean supports(final Service service) {
        return service != null && service.getId().matches(this.casServiceUrlPattern)
            && getEntityIdAsParameter(service).isPresent();
    }
}
