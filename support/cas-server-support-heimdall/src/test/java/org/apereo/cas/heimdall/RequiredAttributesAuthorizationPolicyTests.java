package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.RejectedAttributesAuthorizationPolicy;
import org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAttributesAuthorizationPolicy;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RequiredAttributesAuthorizationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class RequiredAttributesAuthorizationPolicyTests {

    private static AuthorizationRequest authZenRequest() {
        return AuthorizationRequest.builder()
            .subject(AuthZenSubject.builder().type("user").id("alice")
                .properties(Map.of("department", "Sales", "tags", List.of("internal", "beta"), "level", 3)).build())
            .resource(AuthZenResource.builder().type("document").id("doc-1").properties(Map.of("owner", "alice")).build())
            .action(AuthZenAction.builder().name("can_read").properties(Map.of("method", "GET")).build())
            .context(new HashMap<>(Map.of("channel", "web")))
            .principal(RegisteredServiceTestUtils.getPrincipal("alice", Map.of("memberOf", List.of("admin"))))
            .build();
    }

    @ParameterizedTest
    @CsvSource({
        "subject.id,alice",
        "subject.type,user",
        "resource.id,doc-1",
        "resource.type,document",
        "action.name,can_read",
        "subject.properties.department,Sales",
        "subject.properties.tags,beta",
        "subject.properties.level,3",
        "resource.properties.owner,alice",
        "action.properties.method,GET",
        "context.channel,web",
        "memberOf,admin"
    })
    void verifyQualifiedAttributes(final String name, final String value) throws Throwable {
        val request = authZenRequest();
        val resource = new AuthorizableResource();
        assertTrue(new RequiredAttributesAuthorizationPolicy(Map.of(name, Set.of('^' + value + '$')))
            .evaluate(resource, request).authorized());
        assertFalse(new RequiredAttributesAuthorizationPolicy(Map.of(name, Set.of("^unmatched$")))
            .evaluate(resource, request).authorized());
        assertFalse(new RejectedAttributesAuthorizationPolicy().setAttributes(Map.of(name, Set.of('^' + value + '$')))
            .evaluate(resource, request).authorized());
    }

    @Test
    void verifyMissingAttributes() throws Throwable {
        val resource = new AuthorizableResource();
        val legacy = AuthorizationRequest.builder().namespace("API").method("GET").uri("/api")
            .principal(RegisteredServiceTestUtils.getPrincipal("alice")).build();
        for (val name : List.of("subject.id", "subject.properties.department", "resource.properties.owner",
            "action.properties.method", "context.channel", "memberOf")) {
            assertTrue(legacy.resolveAttributeValues(name).isEmpty(), name);
            assertFalse(new RequiredAttributesAuthorizationPolicy(Map.of(name, Set.of(".*")))
                .evaluate(resource, legacy).authorized(), name);
        }
        assertTrue(authZenRequest().resolveAttributeValues("subject.properties.unknown").isEmpty());
    }
}
