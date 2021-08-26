package org.apereo.cas.shell.commands.jasypt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptDecryptPropertyCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class JasyptDecryptPropertyCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(() -> shell.evaluate(() -> "decrypt-value --value {cas-cipher}iARpnWTURDdiAhWdcHXxqJpncj4iRo3w9i2UT33stcs= "
            + "--password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC"));
    }

    @Test
    public void verifyOperationWithInitVector() {
        assertDoesNotThrow(() -> shell.evaluate(() -> "decrypt-value --value {cas-cipher}vGJIJnpRIMoB7cusy2f5ogn9gJI/8n8kBr6D/ce62QjnBLdfe5dcPcNWKL7ypaKp "
                + "--password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC --initvector"));
    }
}


