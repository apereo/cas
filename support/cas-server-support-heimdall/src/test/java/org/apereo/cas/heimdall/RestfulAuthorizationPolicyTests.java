package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy;
import org.apereo.cas.heimdall.authorizer.resource.policy.RestfulAuthorizationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import com.sun.net.httpserver.HttpServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulAuthorizationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class RestfulAuthorizationPolicyTests {

    @Test
    void verifyPoliciesAreNotSent() throws Throwable {
        val body = new AtomicReference<String>();
        val server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            try (val input = exchange.getRequestBody()) {
                body.set(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            val policy = new RestfulAuthorizationPolicy()
                .setUrl("http://127.0.0.1:%s/authorize".formatted(server.getAddress().getPort()));
            val resource = new AuthorizableResource().setId(7).setResourceType("document")
                .setPolicies(List.of(new JdbcAuthorizationPolicy().setPassword("jdbc-secret"), policy));
            val request = AuthorizationRequest.builder().namespace("API").uri("/api").method("GET").build()
                .withPrincipal(RegisteredServiceTestUtils.getPrincipal("casuser"));
            assertTrue(policy.evaluate(resource, request).authorized());
            assertNotNull(body.get());
            assertFalse(body.get().contains("jdbc-secret"));
            assertFalse(body.get().contains("policies"));
            assertTrue(body.get().contains("document"));
        } finally {
            server.stop(0);
        }
    }
}
