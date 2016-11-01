package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.Service;
import org.jasig.cas.client.util.URIBuilder;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is {@link SamlIdPEntityIdValidationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPEntityIdValidationServiceSelectionStrategy implements ValidationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private ServiceFactory webApplicationServiceFactory;
    
    @Override
    public Service resolveServiceFrom(final Service service) {
        final String entityId = getEntityIdAsParameter(service).get().getValue();
        logger.debug("Located entity id {} from service authentication request at {}", entityId, service.getId());
        return this.webApplicationServiceFactory.createService(entityId);
    }

    @Override
    public boolean supports(final Service service) {
        return getEntityIdAsParameter(service).isPresent();
    }

    @Override
    public int compareTo(final ValidationServiceSelectionStrategy o) {
        return 0;
    }

    /**
     * Gets entity id as parameter.
     *
     * @param service the service
     * @return the entity id as parameter
     */
    protected static Optional<URIBuilder.BasicNameValuePair> getEntityIdAsParameter(final Service service) {
        final URIBuilder builder = new URIBuilder(service.getId());
        final Optional<URIBuilder.BasicNameValuePair> param = builder.getQueryParams().stream()
                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID)).findFirst();
        return param;
    }

    public void setWebApplicationServiceFactory(final ServiceFactory webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }
}
