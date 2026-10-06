package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.merger.ReplacingAttributeAdder;
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
        assertTrue(RuntimeHintsPredicates.reflection().onType(DefaultAuthentication.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(AbstractCredential.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(SimplePrincipal.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(UsernamePasswordCredential.class).test(hints));
    }

    @Test
    void verifyAuthenticationResultSerializationHints() {
        val hints = new RuntimeHints();
        new CasCoreAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val type : List.of(DefaultAuthenticationResult.class, DefaultAuthenticationResultBuilder.class,
            DefaultPrincipalElectionStrategy.class, DefaultPrincipalFactory.class, ReplacingAttributeAdder.class)) {
            val typeHint = hints.reflection().getTypeHint(type);
            assertNotNull(typeHint);
            assertTrue(typeHint.hasJavaSerialization());
        }
    }
}
