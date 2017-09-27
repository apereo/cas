package org.apereo.cas.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AbstractPrincipalEditableAttributeValueRepository}.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
public abstract class AbstractPrincipalEditableAttributeValueRepository implements EditableAttributeValueRepository {
    private static final long serialVersionUID = 6523054640180934L;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractPrincipalEditableAttributeValueRepository.class);

    /**
     * Ticket registry support.
     */
    protected final TicketRegistrySupport ticketRegistrySupport;

    public AbstractPrincipalEditableAttributeValueRepository(final TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    @Override
    public Pair<Principal, Map<String, String>> getAttributeValues(final RequestContext requestContext, final Credential credential,
            final Set<String> attributeIds) {
        final Principal principal = WebUtils.getPrincipalFromRequestContext(requestContext, this.ticketRegistrySupport);

        return Pair.of(principal, getPrincipalAttributeValues(principal, attributeIds));
    }

    /**
     * Get attribute values for given principal.
     * 
     * @param principal Principal to retrieve attributes for
     * @param attributeIds Attribute ids to retrieve
     * @return Map of attribute name value pairs
     */
    protected Map<String, String> getPrincipalAttributeValues(final Principal principal, final Set<String> attributeIds) {
        final Map<String, Object> attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);
        final HashMap<String, String> requestedAttributes = new HashMap<>();

        if (attributes != null) {
            attributes.forEach((k, v) -> {
                if (attributeIds.contains(k)) {
                    requestedAttributes.put(k, CollectionUtils.toCollection(v).stream().findFirst().toString());
                }
            });
        }
        return requestedAttributes;
    }

}
