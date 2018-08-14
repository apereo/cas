package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

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
     * @return analysis result
     */
    UmaResourceSetClaimPermissionResult examine(ResourceSet rs, UmaPermissionTicket ticket);
}
