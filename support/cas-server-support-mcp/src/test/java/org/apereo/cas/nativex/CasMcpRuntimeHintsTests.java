package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.mcp.tools.CasSettingTools;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMcpRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class CasMcpRuntimeHintsTests {
    @Test
    void verifyOutputSchemaHints() {
        val hints = new RuntimeHints();
        new CasMcpRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val component : CasSettingTools.SettingSearch.class.getRecordComponents()) {
            assertTrue(RuntimeHintsPredicates.reflection().onMethodInvocation(component.getAccessor()).test(hints));
        }
    }
}
