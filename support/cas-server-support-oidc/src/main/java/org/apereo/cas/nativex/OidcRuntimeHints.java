package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.oidc.claims.OidcRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.ticket.OidcDefaultPushedAuthorizationRequest;
import org.apereo.cas.oidc.token.OidcJwtAccessTokenCipherExecutor;
import org.apereo.cas.oidc.web.response.OidcJwtResponseModeCipherExecutor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link OidcRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, List.of(OidcRegisteredService.class, OidcDefaultPushedAuthorizationRequest.class));
        registerReflectionHints(hints, List.of(
            OidcJsonWebKeystoreGeneratorService.class,
            OidcRegisteredService.class,
            OidcJwtAccessTokenCipherExecutor.class,
            OidcJwtResponseModeCipherExecutor.class
        ));

        val releasePolicies = findSubclassesInPackage(OidcRegisteredServiceAttributeReleasePolicy.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, releasePolicies);
        registerSerializationHints(hints, releasePolicies);
    }

    private static void registerSerializationHints(final RuntimeHints hints, final Collection<Class> entries) {
        entries.forEach(el -> hints.serialization().registerType(el));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        entries.forEach(el -> hints.reflection().registerType((Class) el,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS));
    }
}

