package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedMember;

/**
 * This is {@link GeodeClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class GeodeClusterTopologyManager implements ClusterTopologyManager {
    private final List<Cache> caches;

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val clusterMembers = new ArrayList<ClusterMember>();
        for (val cache : caches) {
            val distributedSystem = cache.getDistributedSystem();
            val localMember = distributedSystem.getDistributedMember();
            val members = new LinkedHashSet<DistributedMember>();
            members.add(localMember);
            members.addAll(distributedSystem.getAllOtherMembers());
            clusterMembers.addAll(members
                .stream()
                .map(member -> toClusterMember(member, localMember, cache))
                .toList());
        }
        return clusterMembers;
    }

    private ClusterMember toClusterMember(
        final DistributedMember member,
        final DistributedMember localMember,
        final Cache cache) {
        val memberName = member.getName();
        val displayName = memberName != null && !memberName.isBlank()
            ? memberName
            : member.getUniqueId();

        return ClusterMember.builder()
            .owner(getName() + '@' + cache.getName())
            .id(member.getUniqueId())
            .address(member.getHost())
            .status(true)
            .description(displayName)
            .attributes(Map.of(
                "cache", cache.getName(),
                "name", displayName,
                "memberId", member.getId(),
                "uniqueId", member.getUniqueId(),
                "host", member.getHost(),
                "processId", member.getProcessId(),
                "groups", member.getGroups(),
                "local", member.equals(localMember)
            ))
            .build();

    }
}
