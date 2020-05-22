package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.TextMagicProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TextMagicSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class TextMagicSmsSenderTests {
    @Test
    public void verifyAction() {
        val props = new TextMagicProperties();
        props.setUsername("casuser");
        props.setPassword("password");
        val sender = new TextMagicSmsSender(props, Optional.empty());
        assertFalse(sender.send("123456678", "123456678", "Msg"));
    }
}
