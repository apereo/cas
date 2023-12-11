package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link InweboAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class InweboAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val list = List.of(XIncludeAwareParserConfiguration.class, DTDDVFactoryImpl.class);
        registerReflectionHint(hints, list);
    }

    private static void registerReflectionHint(final RuntimeHints hints, final Collection clazzes) {
        clazzes.forEach(clazz ->
            hints.reflection().registerType((Class) clazz,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
    }
}
