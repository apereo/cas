package org.apereo.cas.nativex;

import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link WsFederationAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WsFederationAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(WsFederationCredentialsToPrincipalResolver.class));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_DECLARED_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> hints.reflection().registerType((Class) el, memberCategories));
    }
}
