package org.apereo.cas.nativex;

import module java.base;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisModulesRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class RedisModulesRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new RedisModulesRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(RedisModulesCommands.class).test(hints));
    }
}
