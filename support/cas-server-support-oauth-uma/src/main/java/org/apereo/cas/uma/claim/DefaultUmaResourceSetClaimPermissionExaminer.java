package org.apereo.cas.uma.claim;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;

import lombok.val;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DefaultUmaResourceSetClaimPermissionExaminer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultUmaResourceSetClaimPermissionExaminer implements UmaResourceSetClaimPermissionExaminer {
    @Override
    public Map<ResourceSetPolicyPermission, Collection<String>> examine(final ResourceSet rs, final UmaPermissionTicket ticket) {
        val allUnmatched = new LinkedHashMap<ResourceSetPolicyPermission, Collection<String>>();
        for (val policy : rs.getPolicies()) {
            for (val perm : policy.getPermissions()) {
                val unmatched = examinePermission(perm, ticket);
                allUnmatched.put(perm, unmatched);
            }
        }
        return allUnmatched;
    }

    private Collection<String> examinePermission(final ResourceSetPolicyPermission permission, final UmaPermissionTicket ticket) {
        val claimsUnmatched = new HashSet<String>();
        permission.getClaims().forEach((permClaimKey, permClaimValue) -> {
            val matched = ticket.getClaims()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equalsIgnoreCase(permClaimKey) && entry.getValue().equals(permClaimValue));
            if (!matched) {
                claimsUnmatched.add(permClaimKey);
            }
        });
        return claimsUnmatched;
    }
}
