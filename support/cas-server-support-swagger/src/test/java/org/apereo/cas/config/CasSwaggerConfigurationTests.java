package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import springfox.documentation.spring.web.plugins.Docket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSwaggerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = CasSwaggerConfiguration.class)
@Tag("Simple")
public class CasSwaggerConfigurationTests {
    @Autowired
    @Qualifier("api")
    private Docket api;

    @Test
    public void verifyOperation() {
        assertNotNull(api);
    }
}
