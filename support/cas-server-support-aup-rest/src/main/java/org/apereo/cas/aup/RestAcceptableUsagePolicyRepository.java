package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;

/**
 * This is {@link RestAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back via REST.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {

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
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                CollectionUtils.wrap("username", credential.getId()), new HashMap<>());
            val statusCode = response.getStatusLine().getStatusCode();
            return HttpStatus.valueOf(statusCode).is2xxSuccessful();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
