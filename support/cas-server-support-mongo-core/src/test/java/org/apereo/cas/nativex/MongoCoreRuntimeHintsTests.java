package org.apereo.cas.nativex;

import module java.base;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class MongoCoreRuntimeHintsTests {

    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new MongoCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(WriteConcern.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(ReadConcern.class).test(hints));
    }
}

