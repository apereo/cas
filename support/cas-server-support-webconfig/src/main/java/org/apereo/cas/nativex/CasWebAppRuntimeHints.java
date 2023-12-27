package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import java.util.Collection;

/**
 * This is {@link CasWebAppRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasWebAppRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
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
