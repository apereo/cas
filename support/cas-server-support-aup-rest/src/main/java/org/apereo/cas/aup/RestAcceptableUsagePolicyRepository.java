package org.apereo.cas.aup;

import org.apache.http.HttpResponse;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RestAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back via REST.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAcceptableUsagePolicyRepository.class);

    private static final long serialVersionUID = 1600024683199961892L;

    private final AcceptableUsagePolicyProperties.Rest properties;

    public RestAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final String aupAttributeName,
                                               final AcceptableUsagePolicyProperties.Rest restProperties) {
        super(ticketRegistrySupport, aupAttributeName);
        this.properties = restProperties;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            final HttpResponse response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                    properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                    CollectionUtils.wrap("username", credential.getId()));
            return response.getStatusLine().getStatusCode() == HttpStatus.ACCEPTED.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
