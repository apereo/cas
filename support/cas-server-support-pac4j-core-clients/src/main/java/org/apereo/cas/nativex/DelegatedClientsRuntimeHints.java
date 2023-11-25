package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import java.util.Collection;

/**
 * This is {@link DelegatedClientsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DelegatedClientsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerPattern("META-INF/services/com.fasterxml.jackson.databind.Module");
        
        hints.proxies()
            .registerJdkProxy(DelegatedClientFactoryCustomizer.class)
            .registerJdkProxy(DelegatedIdentityProviderFactory.class);

        registerReflectionHints(hints,
            findSubclassesInPackage(DelegatedIdentityProviderFactory.class, CentralAuthenticationService.NAMESPACE));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection clazzes) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS
        };
        clazzes.forEach(entry -> {
            if (entry instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
        });
    }
}
