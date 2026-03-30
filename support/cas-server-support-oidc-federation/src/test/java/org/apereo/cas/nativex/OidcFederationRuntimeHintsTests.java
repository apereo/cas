package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcOpenIdScopeAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link OidcFederationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class OidcFederationRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new OidcFederationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(OidcRegisteredService.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(OidcOpenIdScopeAttributeReleasePolicy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(OidcCustomScopeAttributeReleasePolicy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.serialization().onType(OidcOpenIdScopeAttributeReleasePolicy.class).test(hints));
    }
}
