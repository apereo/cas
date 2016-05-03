package org.jasig.cas.support.saml.services;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.client.util.URIBuilder;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.validation.ValidationServiceSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This is {@link SamlIdPEntityIdValidationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("samlIdPEntityIdValidationServiceSelectionStrategy")
public class SamlIdPEntityIdValidationServiceSelectionStrategy implements ValidationServiceSelectionStrategy {
    private static final long serialVersionUID = -2718740284657155873L;
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    
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
    protected Optional<URIBuilder.BasicNameValuePair> getEntityIdAsParameter(final Service service) {
        final URIBuilder builder = new URIBuilder(service.getId());
        final Optional<URIBuilder.BasicNameValuePair> param = builder.getQueryParams().stream()
                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID)).findFirst();
        return param;
    }
}
