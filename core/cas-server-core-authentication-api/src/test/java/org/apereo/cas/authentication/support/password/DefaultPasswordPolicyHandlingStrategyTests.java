package org.apereo.cas.authentication.support.password;

import org.apereo.cas.DefaultMessageDescriptor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultPasswordPolicyHandlingStrategyTests {

    @Test
    public void verifyOperation() throws Exception {
        val s = new DefaultPasswordPolicyHandlingStrategy<Object>();
        assertTrue(s.handle(new Object(), null).isEmpty());
        val cfg = new PasswordPolicyContext(30);
        cfg.setAccountStateHandler((o, o2) -> List.of(new DefaultMessageDescriptor("bad.password")));
        assertFalse(s.handle(new Object(), cfg).isEmpty());
    }
}
