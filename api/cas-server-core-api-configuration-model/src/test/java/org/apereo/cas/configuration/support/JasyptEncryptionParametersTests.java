package org.apereo.cas.configuration.support;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JasyptEncryptionParametersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("CasConfiguration")
public class JasyptEncryptionParametersTests {

    @Test
    public void verifyOperation() {
        assertNotNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.ALGORITHM.getDefaultValue());

        assertNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PROVIDER.getDefaultValue());
        assertNotNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PROVIDER.getPropertyName());

        assertNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.ITERATIONS.getDefaultValue());
        assertNotNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.ITERATIONS.getPropertyName());

        assertNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getDefaultValue());
        assertNotNull(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName());
    }

}
