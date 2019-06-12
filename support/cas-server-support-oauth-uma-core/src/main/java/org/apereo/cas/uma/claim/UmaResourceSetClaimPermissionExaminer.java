package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

/**
 * This is {@link UmaResourceSetClaimPermissionExaminer}.
 * <p>
 * The authorization server uses the permission ticket to look up the details of the previously registered requested permission,
 * maps the requested permission to operative resource owner policies based on the resource set identifier and scopes associated with it,
 * potentially requests additional information
 * and receives additional information such as claims, and ultimately responds positively or negatively to the request for authorization data.
 * <p>
 * The authorization server bases the issuance of authorization data on resource owner policies. Thus, these policies
 * function as authorization that has been granted ahead of time. The authorization server is also free to enable the resource owner
 * to set policies that require the owner to interact with the server to authorize an access attempt in near-real time, or to help the resource owner field access
 * requests as acts of post hoc authorization. Thus, authorization by UMA methods constitutes an asynchronous authorization grant.
 * All such processes are outside the scope of this specification.
 * <p>
 * Note: If the client incompletely satisfies any policy criteria, the authorization server is free either to partially fulfill
 * the elements of that request, for example, granting authorization to some scopes associated with a requested permission but
 * not all, or to reject the request.
 * <p>
 * The authorization server MUST use a default-deny authorization assessment model in adding authorization data to RPTs, that is,
 * "everything that is not expressly allowed is forbidden" for resource sets that resource servers have registered. Exercise
 * caution in implementing default-deny because corner cases can inadvertently result in default-permit behavior.
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
