package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.jasig.cas.client.util.URIBuilder;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is {@link SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy implements AuthenticationRequestServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy.class);
    
    private ServiceFactory webApplicationServiceFactory;

    public SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy(final ServiceFactory webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
    public Service resolveServiceFrom(final Service service) {
        final String entityId = getEntityIdAsParameter(service).get().getValue();
        LOGGER.debug("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
        return this.webApplicationServiceFactory.createService(entityId);
    }

    @Override
    public boolean supports(final Service service) {
        return getEntityIdAsParameter(service).isPresent();
    }

    @Override
    public int compareTo(final AuthenticationRequestServiceSelectionStrategy o) {
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
}
