package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreAuthenticationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreAuthenticationRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasCoreAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies()
            .forInterfaces(AuthenticationEventExecutionPlanConfigurer.class)
            .test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(SimplePrincipal.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(DefaultAuthentication.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(UsernamePasswordCredential.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(AbstractCredential.class).test(hints));

        assertTrue(RuntimeHintsPredicates.reflection().onType(SimplePrincipal.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(UsernamePasswordCredential.class).test(hints));
    }
}
