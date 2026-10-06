package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.web.view.json.CasJsonServiceResponse;
import org.apereo.cas.web.view.json.CasJsonServiceResponseAuthenticationFailure;
import org.apereo.cas.web.view.json.CasJsonServiceResponseAuthenticationSuccess;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasValidationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class CasValidationRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasValidationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val type : List.of(CasJsonServiceResponse.class,
            CasJsonServiceResponseAuthenticationSuccess.class, CasJsonServiceResponseAuthenticationFailure.class)) {
            assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(type.getConstructor()).test(hints));
        }
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(CasJsonServiceResponse.class.getMethod("getAuthenticationSuccess")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(CasJsonServiceResponseAuthenticationSuccess.class.getMethod("getUser")).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(CasJsonServiceResponseAuthenticationFailure.class.getMethod("getCode")).test(hints));
    }
}
