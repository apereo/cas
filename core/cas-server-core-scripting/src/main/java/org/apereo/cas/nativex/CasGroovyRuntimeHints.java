package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This is {@link CasGroovyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CasGroovyRuntimeHints implements CasRuntimeHintsRegistrar {
    private static final int GROOVY_DGM_CLASS_COUNTER = 1500;
    
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            StaticCompileTransformation.class,
            StaticTypesTransformation.class,
            Script.class,
            BytecodeInterface8.class
        ));
        registerGroovyDGMClasses(hints, classLoader);
    }

    private static void registerGroovyDGMClasses(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        IntStream.range(1, GROOVY_DGM_CLASS_COUNTER).forEach(idx ->
            hints.reflection().registerTypeIfPresent(classLoader,
                "org.codehaus.groovy.runtime.dgm$" + idx,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.ACCESS_PUBLIC_FIELDS));
    }
}
