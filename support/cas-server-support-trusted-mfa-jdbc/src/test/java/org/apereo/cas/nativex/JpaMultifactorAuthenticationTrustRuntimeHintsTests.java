package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.generic.JpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.oracle.OracleJpaMultifactorAuthenticationTrustRecord;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.support.SpringFactoriesLoader;
import jakarta.persistence.Id;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests native metadata for trusted-device JPA entities.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class JpaMultifactorAuthenticationTrustRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JpaMultifactorAuthenticationTrustRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val type : List.of(JpaMultifactorAuthenticationTrustRecord.class, OracleJpaMultifactorAuthenticationTrustRecord.class)) {
            val identifier = type.getDeclaredField("id");
            assertTrue(identifier.isAnnotationPresent(Id.class));
            assertTrue(RuntimeHintsPredicates.reflection().onFieldAccess(identifier).test(hints));
            assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(type.getConstructor()).test(hints));
            assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(type.getMethod("setId", long.class)).test(hints));
        }
        for (val field : List.of("principal", "deviceFingerprint", "recordDate", "recordKey", "name",
            "expirationDate", "multifactorAuthenticationProvider")) {
            assertTrue(RuntimeHintsPredicates.reflection()
                .onFieldAccess(MultifactorAuthenticationTrustRecord.class.getDeclaredField(field)).test(hints), field);
        }
    }

    @Test
    void verifyRegistration() {
        val registrars = SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories", getClass().getClassLoader())
            .load(RuntimeHintsRegistrar.class);
        assertTrue(registrars.stream().anyMatch(JpaMultifactorAuthenticationTrustRuntimeHints.class::isInstance));
    }
}
