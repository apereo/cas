package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebApplicationAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTest(classes = CasWebApplicationAutoConfiguration.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
class CasWebApplicationAutoConfigurationTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val event = new ApplicationReadyEvent(
            mock(SpringApplication.class), ArrayUtils.EMPTY_STRING_ARRAY,
            applicationContext, Duration.ofSeconds(1));
        assertDoesNotThrow(() -> applicationContext.publishEvent(event));
    }
}
