package org.apereo.cas.uma.ticket.permission;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;
import java.util.Set;

/**
 * This is {@link UmaPermissionTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface UmaPermissionTicket extends Ticket {
    /**
     * Prefix generally applied to unique ids.
     */
    String PREFIX = "UMAP";

    /**
     * Gets supplied claims.
     *
     * @return the supplied claims
     */
    Map<String, Object> getClaims();

    /**
     * Gets scopes.
     *
     * @return the scopes
     */
    Set<String> getScopes();

    /**
     * Gets resource set.
     *
     * @return the resource set
     */
    ResourceSet getResourceSet();
}
