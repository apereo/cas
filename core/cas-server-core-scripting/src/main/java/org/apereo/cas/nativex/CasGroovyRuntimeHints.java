package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import groovy.json.JsonSlurper;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.transform.stc.ClosureSignatureHint;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromAbstractTypeMethods;
import groovy.transform.stc.FromString;
import groovy.transform.stc.MapEntryOrKeyValue;
import groovy.transform.stc.SecondParam;
import groovy.transform.stc.SimpleType;
import groovy.transform.stc.ThirdParam;
import org.codehaus.groovy.runtime.BytecodeInterface8;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasGroovyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CasGroovyRuntimeHints implements CasRuntimeHintsRegistrar {
    private static final int GROOVY_DGM_CLASS_COUNTER = 1500;
    
    @Override
    public void registerHints(final RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            Stack.class,
            GroovyClassLoader.class,
            JsonSlurper.class,
            StaticTypeCheckingSupport.ObjectArrayStaticTypesHelper.class,
            StaticCompileTransformation.class,
            StaticTypesTransformation.class,
            Script.class,
            BytecodeInterface8.class
        ));
        Stream.of(FirstParam.class, SecondParam.class, ThirdParam.class, FromAbstractTypeMethods.class,
                FromString.class, MapEntryOrKeyValue.class, SimpleType.class)
            .flatMap(type -> Stream.concat(Stream.of(type), Arrays.stream(type.getDeclaredClasses())))
            .filter(ClosureSignatureHint.class::isAssignableFrom)
            .forEach(type -> hints.reflection().registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
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
