package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAuthorizationPolicyAuthZenTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class JdbcAuthorizationPolicyAuthZenTests {

    @Test
    void verifyAuthZenParameters() throws Throwable {
        val policy = new JdbcAuthorizationPolicy()
            .setUrl("jdbc:hsqldb:mem:" + UUID.randomUUID())
            .setUsername("sa")
            .setPassword(StringUtils.EMPTY)
            .setQuery("""
                SELECT authorized FROM heimdall_grants
                WHERE subject_type = :subjectType AND subject_id = :subjectId
                AND resource_type = :resourceType AND resource_id = :resourceId
                AND permission = :action AND principal_id = :principal
                """);
        val template = policy.buildJdbcTemplate();
        policy.setJdbcTemplate(template);
        try {
            template.getJdbcOperations().execute("""
                CREATE TABLE heimdall_grants (subject_type VARCHAR(50), subject_id VARCHAR(50),
                resource_type VARCHAR(50), resource_id VARCHAR(50), permission VARCHAR(50),
                principal_id VARCHAR(50), authorized BOOLEAN)
                """);
            template.getJdbcOperations().execute(
                "INSERT INTO heimdall_grants VALUES ('user', 'alice', 'account', '123', 'can_read', 'alice', TRUE)");

            val request = AuthorizationRequest.builder()
                .subject(AuthZenSubject.builder().type("user").id("alice").build())
                .resource(AuthZenResource.builder().type("account").id("123").build())
                .action(AuthZenAction.builder().name("can_read").build())
                .principal(RegisteredServiceTestUtils.getPrincipal("alice", Map.of()))
                .build();
            val resource = new AuthorizableResource();
            assertTrue(policy.evaluate(resource, request).authorized());

            val otherAction = request.withAction(AuthZenAction.builder().name("can_delete").build());
            assertFalse(policy.evaluate(resource, otherAction).authorized());
            assertFalse(policy.evaluate(resource, otherAction.withContext(Map.of("action", "can_read"))).authorized());
            assertFalse(policy.evaluate(resource, request.withResource(
                AuthZenResource.builder().type("account").id("456").build())).authorized());
        } finally {
            template.getJdbcOperations().execute("SHUTDOWN");
        }
    }
}
