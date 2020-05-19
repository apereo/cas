package org.apereo.cas.services.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceYamlSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("Simple")
public class RegisteredServiceYamlSerializerTests {

    @Test
    public void verifyPrinter() {
        val zer = new RegisteredServiceYamlSerializer();
        assertFalse(zer.supports(new File("bad-file")));
    }
}
