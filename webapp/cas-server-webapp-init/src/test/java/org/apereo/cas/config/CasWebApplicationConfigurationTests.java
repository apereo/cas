package org.apereo.cas.config;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebApplicationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTest(classes = CasWebApplicationConfiguration.class)
@Tag("CasConfiguration")
class CasWebApplicationConfigurationTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val event = new ApplicationReadyEvent(
            mock(SpringApplication.class), ArrayUtils.EMPTY_STRING_ARRAY,
            applicationContext, Duration.ofSeconds(1));
        assertDoesNotThrow(() -> applicationContext.publishEvent(event));
    }
}
