package org.apereo.cas.heimdall;

import module java.base;
import module java.sql;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.DefaultResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.ResourceAuthorizationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultResourceAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class DefaultResourceAuthorizerTests {
    private static final ResourceAuthorizationPolicy GRANT = (resource, request) -> AuthorizationResult.granted("OK");

    private static final ResourceAuthorizationPolicy DENY = (resource, request) -> AuthorizationResult.denied("Denied");

    private static final ResourceAuthorizationPolicy FAIL = (resource, request) -> {
        throw new SQLTimeoutException("Query timed out");
    };

    private final DefaultResourceAuthorizer authorizer = new DefaultResourceAuthorizer();

    private static AuthorizationRequest request() {
        return AuthorizationRequest.builder().namespace("API").method("GET").uri("/api")
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser")).build();
    }

    private static AuthorizableResource resource(final boolean enforceAllPolicies, final ResourceAuthorizationPolicy... policies) {
        return new AuthorizableResource().setId(1).setEnforceAllPolicies(enforceAllPolicies).setPolicies(List.of(policies));
    }

    @Test
    void verifyFailingPolicyDoesNotBlockLaterGrant() {
        assertTrue(authorizer.evaluate(request(), resource(false, FAIL, DENY, GRANT)).authorized());
    }

    @Test
    void verifyFailuresAreReportedWhenNothingGrants() {
        val exception = assertThrows(IllegalStateException.class,
            () -> authorizer.evaluate(request(), resource(false, FAIL, DENY, FAIL)));
        assertInstanceOf(SQLTimeoutException.class, exception.getCause());
        assertEquals(1, exception.getSuppressed().length);
    }

    @Test
    void verifyDenialWithoutFailures() {
        assertFalse(authorizer.evaluate(request(), resource(false, DENY, DENY)).authorized());
    }

    @Test
    void verifyAllPoliciesFailOnFirstError() {
        assertThrows(RuntimeException.class, () -> authorizer.evaluate(request(), resource(true, GRANT, FAIL)));
        assertFalse(authorizer.evaluate(request(), resource(true, DENY, FAIL)).authorized());
    }
}
