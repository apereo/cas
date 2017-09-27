package org.apereo.cas.attributes;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RestEditableAttributeValueRepository}.
 * Stores editable attribute values in configurable REST interface.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
public class RestEditableAttributeValueRepository extends AbstractPrincipalEditableAttributeValueRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEditableAttributeValueRepository.class);

    private static final long serialVersionUID = 1011609856110633L;

    private final EditableAttributeProperties.Rest properties;

    public RestEditableAttributeValueRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final EditableAttributeProperties.Rest restProperties) {
        super(ticketRegistrySupport);
        this.properties = restProperties;
    }

    @Override
	public boolean storeAttributeValues(RequestContext requestContext, Credential credential,
			Map<String, String> attributeValues) {

    	Map<String,String> attributeMap = new HashMap<>( attributeValues );
    	attributeMap.put("username", credential.getId());

    	try {
            final HttpResponse response = HttpUtils.execute(properties.getUrl(), properties.getMethod(),
                    properties.getBasicAuthUsername(), properties.getBasicAuthPassword(),
                    attributeMap);
            return response.getStatusLine().getStatusCode() == HttpStatus.ACCEPTED.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
