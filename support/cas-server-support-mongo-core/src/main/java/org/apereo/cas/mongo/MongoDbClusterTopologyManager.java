package org.apereo.cas.mongo;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

/**
 * This is {@link MongoDbClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class MongoDbClusterTopologyManager implements ClusterTopologyManager {
    private final MongoOperations mongoTemplate;

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val adminDatabase = ((MongoTemplate) mongoTemplate).getMongoDatabaseFactory().getMongoDatabase("admin");
        val helloResult = adminDatabase.runCommand(new Document("hello", 1));
        val replicaSetName = helloResult.getString("setName");
        if (StringUtils.hasText(replicaSetName)) {
            val status = adminDatabase.runCommand(new Document("replSetGetStatus", 1));
            val members = status.getList("members", Document.class, List.of());
            return members
                .stream()
                .map(member -> ClusterMember.builder()
                    .owner(getName())
                    .id(String.valueOf(member.getInteger("_id")))
                    .address(member.getString("name"))
                    .status(member.getDouble("health") == 1)
                    .responseTime(member.get("pingMs") instanceof final Number pingMs ? pingMs.longValue() : -1)
                    .description(member.getString("lastHeartbeatMessage"))
                    .attributes(Map.of("state", member.getString("stateStr"), "self", member.getBoolean("self", false)))
                    .build())
                .toList();
        }
        return List.of();
    }
}
