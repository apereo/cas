package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.http.HttpMethod;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link AMQPClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class AMQPClusterTopologyManager implements ClusterTopologyManager {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String SPRING_RABBITMQ_MANAGEMENT_URL = "spring.rabbitmq.management-url";
    private final CasConfigurationProperties casProperties;
    private final RabbitProperties rabbitProperties;

    @Override
    public List<? extends ClusterMember> discoverMembers() throws Exception {
        val props = casProperties.getTicket().getRegistry().getInMemory().getProperties();
        if (!props.containsKey(SPRING_RABBITMQ_MANAGEMENT_URL)) {
            return List.of();
        }

        val url = props.get(SPRING_RABBITMQ_MANAGEMENT_URL) + "/api/nodes";
        val response = HttpUtils.execute(HttpExecutionRequest.builder()
            .url(url)
            .parameters(Map.of("disable_stats", "true"))
            .basicAuthUsername(rabbitProperties.getUsername())
            .basicAuthPassword(rabbitProperties.getPassword())
            .method(HttpMethod.GET)
            .build());
        try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
            val results = MAPPER.readValue(IOUtils.toString(content, StandardCharsets.UTF_8), new TypeReference<List<RabbitEntry>>() {
            });
            return results
                .stream()
                .map(entry -> ClusterMember.builder()
                    .owner(getName())
                    .id(UUID.randomUUID().toString())
                    .address(entry.name())
                    .attributes(Map.of(
                        "type", entry.type(),
                        "running", entry.running()
                    ))
                    .build())
                .toList();
        }
    }

    private record RabbitEntry(String name, String type, boolean running) {
    }
}
