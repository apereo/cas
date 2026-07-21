package org.apereo.cas.logging;

import module java.base;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasGoogleCloudLoggingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.Severity;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link GoogleCloudLogsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    GoogleCloudLogsEndpointTests.GoogleCloudTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasGoogleCloudLoggingAutoConfiguration.class
}, properties = {
    "cas.logging.gcp.log-name=projects/cas-project-id/logs/cas-server",
    "cas.logging.gcp.project-id=hello-gkej2ee-test3",
    "cas.logging.gcp.labels.namespace_name=cas-idp-0-develop",
    "management.endpoint.gcpLogs.access=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GoogleCloudLogsEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get("/actuator/gcpLogs/stream")
                .param("count", "20")
                .param("level", "info")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @TestConfiguration(value = "GoogleCloudTestConfiguration", proxyBeanMethods = false)
    static class GoogleCloudTestConfiguration {
        @Bean
        public Logging googleCloudLoggingService() {
            val payload = Payload.StringPayload.of("This is a log statement");
            val logPage = mock(Page.class);
            val logEntry = LogEntry.of(payload)
                .toBuilder()
                .setLogName("log-name")
                .setTimestamp(Instant.now(Clock.systemUTC()))
                .setSeverity(Severity.DEBUG)
                .build();
            when(logPage.iterateAll()).thenReturn(List.of(logEntry));
            val logging = mock(Logging.class);
            when(logging.listLogEntries(any(), any(), any())).thenReturn(logPage);
            return logging;
        }
    }
}
