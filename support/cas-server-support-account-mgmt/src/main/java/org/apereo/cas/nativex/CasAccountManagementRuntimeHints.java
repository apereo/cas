package org.apereo.cas.nativex;

import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasAccountManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasAccountManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val entries = List.<Class>of(AccountRegistrationProvisionerConfigurer.class);
        registerProxyHints(hints, entries);
        registerReflectionHints(hints, entries);
    }

    private static void registerProxyHints(final RuntimeHints hints, final Collection<Class> subclassesInPackage) {
        subclassesInPackage.forEach(clazz -> hints.proxies().registerJdkProxy(clazz));
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
