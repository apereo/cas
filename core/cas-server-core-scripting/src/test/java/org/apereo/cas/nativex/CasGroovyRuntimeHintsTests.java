package org.apereo.cas.nativex;

import module java.base;
import groovy.json.JsonSlurper;
import groovy.lang.GroovyClassLoader;
import lombok.val;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasGroovyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Native")
class CasGroovyRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasGroovyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(GroovyClassLoader.class.getConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(JsonSlurper.class.getConstructor()).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(StaticTypeCheckingSupport.ObjectArrayStaticTypesHelper.class.getMethod("getAt", Object[].class, int.class)).test(hints));
    }
}
