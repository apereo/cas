package org.apereo.cas.support.saml.idp;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.time.Duration;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSamlIdPCasEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAMLMetadata")
@Import(DefaultSamlIdPCasEventListenerTests.SamlIdPLocatorFailsTestConfiguration.class)
class DefaultSamlIdPCasEventListenerTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifyPassOperation() {
        System.setProperty("test.runtime", "pass");
        val event = new ApplicationReadyEvent(mock(SpringApplication.class),
            ArrayUtils.EMPTY_STRING_ARRAY, this.applicationContext, Duration.ofSeconds(30));
        assertDoesNotThrow(() -> applicationContext.publishEvent(event));
    }

    @Test
    void verifyFailOperation() {
        System.setProperty("test.runtime", "fail");
        val event = new ApplicationReadyEvent(mock(SpringApplication.class),
            ArrayUtils.EMPTY_STRING_ARRAY, this.applicationContext, Duration.ofSeconds(30));
        assertDoesNotThrow(() -> applicationContext.publishEvent(event));
    }

    @TestConfiguration(value = "SamlIdPLocatorFailsTestConfiguration", proxyBeanMethods = false)
    static class SamlIdPLocatorFailsTestConfiguration {
        @Bean
        public SamlIdPMetadataLocator samlIdPMetadataLocator() throws Throwable {
            val locator = mock(SamlIdPMetadataLocator.class);
            when(locator.exists(argThat(Optional::isEmpty))).thenAnswer((Answer<Boolean>) invocationOnMock -> {
                var property = System.getProperty("test.runtime");
                if (property != null && "fail".equals(property)) {
                    throw new RuntimeException("Failed");
                }
                return true;
            });
            return locator;
        }
    }
}
