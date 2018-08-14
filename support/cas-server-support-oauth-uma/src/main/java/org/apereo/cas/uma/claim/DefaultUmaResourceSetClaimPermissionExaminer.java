package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DefaultUmaResourceSetClaimPermissionExaminer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class DefaultUmaResourceSetClaimPermissionExaminer implements UmaResourceSetClaimPermissionExaminer {
    @Override
    public UmaResourceSetClaimPermissionResult examine(final ResourceSet rs, final UmaPermissionTicket ticket) {
        val result = new UmaResourceSetClaimPermissionResult();
        for (val policy : rs.getPolicies()) {
            val details = new UmaResourceSetClaimPermissionResult.Details();

            for (val permission : policy.getPermissions()) {
                if (!permission.getScopes().containsAll(ticket.getScopes())) {
                    LOGGER.debug("Policy permission [{}] does not contain all requested scopes [{}] from ticket [{}]",
                        permission.getId(), ticket.getScopes(), ticket.getId());
                    val delta = Sets.difference(permission.getScopes(), ticket.getScopes());
                    details.getUnmatchedScopes().addAll(delta);
                }

                permission.getClaims().forEach((permClaimKey, permClaimValue) -> {
                    val matched = ticket.getClaims()
                        .entrySet()
                        .stream()
                        .anyMatch(entry -> entry.getKey().equalsIgnoreCase(permClaimKey) && entry.getValue().equals(permClaimValue));
                    if (!matched) {
                        LOGGER.debug("Policy permission [{}] does not contain all requested claims [{}] from ticket [{}]",
                            permission.getId(), ticket.getClaims(), ticket.getId());
                        details.getUnmatchedClaims().put(permClaimKey, permClaimValue);
                    }
                });

                if (!details.isSatisfied()) {
                    result.getDetails().put(permission.getId(), details);
                }
            }
        }
        return result;
    }
}
