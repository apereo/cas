package org.apereo.cas.attributes;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultEditableAttributeValueRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultEditableAttributeValueRepository extends AbstractPrincipalEditableAttributeValueRepository {

    private static final long serialVersionUID = -6579869788970665L;

    private final Map<String, Map<String,String>> attributeMap = new ConcurrentHashMap<>();

    public DefaultEditableAttributeValueRepository(final TicketRegistrySupport ticketRegistrySupport) {
        super(ticketRegistrySupport);
    }

    @Override
    public boolean storeAttributeValues(final RequestContext requestContext, final Credential credential, Map<String,String> attributeValues) {
    	this.attributeMap.put(credential.getId(), attributeValues);
        return this.attributeMap.containsKey(credential.getId());
    }

	@Override
    protected Map<String,String> getPrincipalAttributeValues(Principal principal, Set<String> attributeIds) {
        final String key = principal.getId();
		if (this.attributeMap.containsKey(key)) {
			return attributeMap.get(key);
		}
		return null;
	}

}
