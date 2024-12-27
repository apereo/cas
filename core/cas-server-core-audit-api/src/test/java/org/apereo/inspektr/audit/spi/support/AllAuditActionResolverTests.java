package org.apereo.inspektr.audit.spi.support;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * This is {@link AllAuditActionResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class AllAuditActionResolverTests extends BaseAuditResolverTests {
    @Test
    void verifyBoolean() {
        val audit = getAuditAnnotation();
        val resolver = new BooleanAuditActionResolver("PASS", "FAIL");
        verifyAuditActionResolver(resolver, audit);
    }

    @Test
    void verifyDefault() {
        val audit = getAuditAnnotation();
        val resolver = new DefaultAuditActionResolver();
        verifyAuditActionResolver(resolver, audit);
    }

    @Test
    void verifyObjectCreation() {
        val audit = getAuditAnnotation();
        val resolver = new ObjectCreationAuditActionResolver("PASS", "FAIL");
        verifyAuditActionResolver(resolver, audit);
    }
}
