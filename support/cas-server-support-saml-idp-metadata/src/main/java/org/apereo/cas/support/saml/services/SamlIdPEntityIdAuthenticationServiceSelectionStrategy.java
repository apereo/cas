package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.util.URIBuilder;

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
public class SamlIdPEntityIdAuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    private final String casServiceUrlPattern;

    public SamlIdPEntityIdAuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        final String casServerPrefix) {
        super(servicesManager, webApplicationServiceFactory);
        this.casServiceUrlPattern = "^".concat(casServerPrefix).concat(".*");
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        val entityId = getEntityIdAsParameter(service).orElseThrow().getValue();
        LOGGER.trace("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
        return createService(entityId, service);
    }

    @Override
    public boolean supports(final Service service) {
        return service != null && service.getId().matches(this.casServiceUrlPattern)
            && getEntityIdAsParameter(service).isPresent();
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
}
