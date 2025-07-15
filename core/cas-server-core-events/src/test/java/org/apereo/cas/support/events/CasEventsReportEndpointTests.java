package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import javax.security.auth.login.FailedLoginException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasEventsReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@AutoConfigureMockMvc
@SpringBootTest(classes = {
    CasEventsReportEndpointTests.CasEventsReportEndpointTestConfiguration.class,
    AbstractCasEventRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "spring.security.user.name=casuser",
    "spring.security.user.password=Mellon",
    "management.endpoint.events.access=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
class CasEventsReportEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @BeforeEach
    void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.100");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() throws Throwable {
        publishEvent();
        Awaitility.await().untilAsserted(() -> assertFalse(casEventRepository.load().findAny().isEmpty()));

        mockMvc.perform(get("/actuator/events")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_NDJSON_VALUE)
                .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon"))
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyAggregate() throws Throwable {
        publishEvent();
        Awaitility.await().untilAsserted(() -> assertFalse(casEventRepository.load().findAny().isEmpty()));
        mockMvc.perform(get("/actuator/events/aggregate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_NDJSON_VALUE)
                .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon")))
            .andExpect(status().isOk());
    }


    @Test
    void verifyImportOperationAsJson() throws Throwable {
        val event = new CasEvent()
            .setId(System.currentTimeMillis())
            .setPrincipalId("casuser")
            .setType(CasAuthenticationTransactionFailureEvent.class.getSimpleName())
            .putClientIpAddress("127.0.0.1")
            .putServerIpAddress("127.0.0.2")
            .putAgent("Firefox")
            .putTimestamp(System.currentTimeMillis());
        val content = MAPPER.writeValueAsString(event);

        mockMvc.perform(post("/actuator/events")
                .with(csrf())
                .content(content)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon"))
            )
            .andExpect(status().isOk());

    }


    @Test
    void verifyBulkImportAsZip() throws Throwable {
        mockMvc.perform(delete("/actuator/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon"))
            )
            .andExpect(status().isOk());

        try (val out = new ByteArrayOutputStream(2048);
             val zipStream = new ZipOutputStream(out)) {
            val event = new CasEvent()
                .setId(System.currentTimeMillis())
                .setPrincipalId("casuser")
                .putEventId(UUID.randomUUID().toString())
                .setType(CasAuthenticationTransactionFailureEvent.class.getSimpleName())
                .putClientIpAddress("127.0.0.1")
                .putServerIpAddress("127.0.0.2")
                .putAgent("Firefox")
                .setCreationTime(ZonedDateTime.now(Clock.systemUTC()).toInstant())
                .putTimestamp(System.currentTimeMillis());
            val content = MAPPER.writeValueAsString(event);
            val name = event.getEventId() + ".json";
            val entry = new ZipEntry(name);
            zipStream.putNextEntry(entry);

            val data = content.getBytes(StandardCharsets.UTF_8);
            zipStream.write(data, 0, data.length);
            zipStream.closeEntry();

            mockMvc.perform(post("/actuator/events")
                    .with(csrf())
                    .content(out.toByteArray())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(HttpUtils.createBasicAuthHeaders("casuser", "Mellon"))
                )
                .andExpect(status().isOk());
        }
    }


    private void publishEvent() {
        val failureEvent = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()), null);
        applicationContext.publishEvent(failureEvent);
        val successEvent = new CasAuthenticationTransactionSuccessfulEvent(this,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
        applicationContext.publishEvent(successEvent);
    }

    @TestConfiguration(value = "CasEventsReportEndpointTestConfiguration", proxyBeanMethods = false)
    static class CasEventsReportEndpointTestConfiguration {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public CasEvent saveInternal(final CasEvent event) {
                    events.add(event);
                    return event;
                }

                @Override
                public void removeAll() {
                    events.clear();
                }

                @Override
                public Stream<CasEvent> load() {
                    return events.stream();
                }

                @Override
                public Stream<CasEventAggregate> aggregate(final Class type, final Duration start) {
                    val now = LocalDateTime.now(Clock.systemUTC());
                    return Stream.of(
                        new CasEventAggregate(now, "type1", 1L, "CAS"),
                        new CasEventAggregate(now, "type2", 1L, "CAS"),
                        new CasEventAggregate(now, "type3", 1L, "Shire"),
                        new CasEventAggregate(now, "type4", 1L, "Shire"));
                }
            };
        }
    }
}
