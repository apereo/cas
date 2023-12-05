package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.util.CollectionUtils;
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
    public UmaResourceSetClaimPermissionResult examine(final UmaPermissionTicket ticket) {
        val result = new UmaResourceSetClaimPermissionResult();
        for (val policy : ticket.getResourceSet().getPolicies()) {
            val details = new UmaResourceSetClaimPermissionResult.Details();

            for (val permission : policy.getPermissions()) {
                LOGGER.debug("Checking permission [{}]", permission.getId());
                if (!ticket.getScopes().containsAll(permission.getScopes())) {
                    LOGGER.warn("Permission ticket [{}] does not contain all needed scopes [{}] required by policy permission [{}]",
                        ticket.getId(), permission.getScopes(), permission.getId());
                    val delta = Sets.difference(ticket.getScopes(), permission.getScopes());
                    details.getUnmatchedScopes().addAll(delta);
                }

                permission.getClaims()
                    .forEach((permClaimKey, permClaimValue) -> {
                        val claimValueToCheck = CollectionUtils.toCollection(permClaimValue);
                        LOGGER.debug("Checking permission claim [{}] with value(s) [{}]", permClaimKey, claimValueToCheck);
                        
                        val matched = ticket.getClaims()
                            .entrySet()
                            .stream()
                            .anyMatch(entry -> entry.getKey().equalsIgnoreCase(permClaimKey)
                                               && CollectionUtils.toCollection(entry.getValue()).containsAll(claimValueToCheck));
                        if (!matched) {
                            LOGGER.warn("Policy permission [{}] does not contain all requested claims [{}] from ticket [{}]",
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
