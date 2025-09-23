package org.apereo.cas.config;

import org.apereo.cas.test.CasTestExtension;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@SpringBootTest(classes = CasSwaggerAutoConfiguration.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasSwaggerConfigurationTests {
    @Autowired
    @Qualifier("casSwaggerOpenApi")
    private OpenAPI swaggerOpenApi;

    @Test
    void verifyOperation() {
        assertNotNull(swaggerOpenApi);
    }
}
