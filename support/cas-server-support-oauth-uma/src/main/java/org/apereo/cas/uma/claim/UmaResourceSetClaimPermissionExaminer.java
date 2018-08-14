package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link UmaResourceSetClaimPermissionExaminer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface UmaResourceSetClaimPermissionExaminer {
    /**
     * Examine.
     *
     * @param rs     the resource
     * @param ticket the ticket
     * @return a map of unmatched permissions linked to unmatched claims.
     */
    Map<ResourceSetPolicyPermission, Collection<String>> examine(ResourceSet rs, UmaPermissionTicket ticket);
}
