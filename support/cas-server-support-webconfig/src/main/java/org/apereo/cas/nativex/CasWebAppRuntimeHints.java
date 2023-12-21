package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.security.web.access.HandlerMappingIntrospectorRequestTransformer;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasWebAppRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.serialization().registerType(DefaultCsrfToken.class);
        registerReflectionHints(hints, List.of(
            DefaultCsrfToken.class,
            BasicAuthenticationFilter.class,
            HandlerMappingIntrospectorRequestTransformer.class,
            TypeReference.of("org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration$HandlerMappingIntrospectorCachFilterFactoryBean"),
            TypeReference.of("org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration$CompositeFilterChainProxy")
        ));
        registerReflectionHints(hints, findSubclassesInPackage(CsrfToken.class, CsrfToken.class.getPackageName()));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> {
            if (el instanceof final String clazz) {
                hints.reflection().registerType(TypeReference.of(clazz), memberCategories);
            }
            if (el instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
            if (el instanceof final TypeReference reference) {
                hints.reflection().registerType(reference, memberCategories);
            }
        });
    }
}
