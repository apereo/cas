package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * This is {@link AbstractPrincipalAttributeAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public abstract class AbstractPrincipalAttributeAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1883808902502739L;

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Single-valued attribute in LDAP that describes whether the policy
     * has been accepted. Its value must match either TRUE/FALSE.
     */
    protected String aupAttributeName;
    
    private TicketRegistrySupport ticketRegistrySupport;

    @Override
    public boolean verify(final RequestContext requestContext, final Credential credential) {
        final Principal principal = getPrincipal(requestContext);
        final Map<String, Object> attributes = principal.getAttributes();
        logger.debug("Principal attributes found for {} are {}", principal.getId(), attributes);

        if (attributes != null && attributes.containsKey(this.aupAttributeName)) {
            final Object value = attributes.get(this.aupAttributeName);
            logger.debug("Evaluating attribute value {} found for {}", value, this.aupAttributeName);
            if (value.toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
                return true;
            }
        }

        logger.warn("Usage policy has not been accepted by {}", principal.getId());
        return false;
    }

    public void setAupAttributeName(final String aupAttributeName) {
        this.aupAttributeName = aupAttributeName;
    }

    public void setTicketRegistrySupport(final TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    /**
     * Gets the authenticated principal.
     *
     * @param requestContext the request context
     * @return the principal
     */
    protected Principal getPrincipal(final RequestContext requestContext) {
        final String tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgt)) {
            throw new IllegalArgumentException("No ticket-granting ticket could be found in the context");
        }

        return this.ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgt);
    }
}
