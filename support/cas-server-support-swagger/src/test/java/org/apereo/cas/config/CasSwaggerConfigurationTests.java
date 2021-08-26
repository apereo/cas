package org.apereo.cas.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSwaggerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = CasSwaggerConfiguration.class)
@Tag("CasConfiguration")
public class CasSwaggerConfigurationTests {
    @Autowired
    @Qualifier("casSwaggerOpenApi")
    private OpenAPI swaggerOpenApi;

    @Test
    public void verifyOperation() {
        assertNotNull(swaggerOpenApi);
    }
}
