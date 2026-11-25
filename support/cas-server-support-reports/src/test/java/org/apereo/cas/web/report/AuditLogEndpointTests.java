package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.spi.AuditActionDateProvider;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link AuditLogEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = {
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "cas.audit.engine.number-of-days-in-history=30",
        "management.endpoint.auditLog.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ActuatorEndpoint")
class AuditLogEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @Test
    void verifyForbiddenOperation() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val result = MAPPER.readValue(mockMvc.perform(get("/actuator/auditLog")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertFalse(result.isEmpty());
    }

    @Test
    void verifyOperationByInterval() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        assertFalse(MAPPER.readValue(mockMvc.perform(get("/actuator/auditLog")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("interval", "PT1H")
                .queryParam("actionPerformed", AuditableActions.SAVE_SERVICE + ".*")
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifyCountAppliesWithInterval() throws Throwable {
        for (var i = 0; i < 3; i++) {
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        }
        val records = (List<Map<String, Object>>) MAPPER.readValue(mockMvc.perform(get("/actuator/auditLog")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("interval", "PT1H")
                .queryParam("count", "2")
                .queryParam("actionPerformed", AuditableActions.SAVE_SERVICE + ".*")
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertEquals(2, records.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifySummary() throws Throwable {
        val action = "AUTHENTICATION_SUCCESS";
        val plan = applicationContext.getBean(AuditTrailExecutionPlan.BEAN_NAME, AuditTrailExecutionPlan.class);
        plan.record(new AuditActionContext("casuser", "resource", action, "CAS",
            applicationContext.getBeanProvider(AuditActionDateProvider.class).getObject().get(), new ClientInfo("1.2.3.4", "1.2.3.4", "agent", "London")));
        val summary = MAPPER.readValue(mockMvc.perform(get("/actuator/auditLog/summary")
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("interval", "PT2H")
                .queryParam("actions", action, "AUTHENTICATION_FAILED"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), Map.class);
        assertEquals(Boolean.TRUE, summary.get("supported"));
        val counts = (List<Map<String, Object>>) summary.get("counts");
        assertTrue(counts.stream().anyMatch(count -> action.equals(count.get("action"))));
        assertTrue(counts.stream().allMatch(count -> ((Number) count.get("count")).longValue() > 0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifySummaryByTarget() throws Throwable {
        val plan = applicationContext.getBean(AuditTrailExecutionPlan.BEAN_NAME, AuditTrailExecutionPlan.class);
        val now = applicationContext.getBeanProvider(AuditActionDateProvider.class).getObject().get();
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", "agent", "London");
        plan.record(new AuditActionContext("casuser", "resource", "AUTHENTICATION_EVENT_TRIGGERED", "CAS", now, clientInfo, "mfa-duo"));
        plan.record(new AuditActionContext("casuser2", "resource", "AUTHENTICATION_EVENT_TRIGGERED", "CAS", now, clientInfo, "mfa-duo"));
        plan.record(new AuditActionContext("casuser", "resource", "AUTHENTICATION_EVENT_TRIGGERED", "CAS", now, clientInfo));
        val summary = MAPPER.readValue(mockMvc.perform(get("/actuator/auditLog/summary")
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("interval", "PT2H")
                .queryParam("actions", "AUTHENTICATION_EVENT_TRIGGERED")
                .queryParam("groupBy", "target"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), Map.class);
        assertEquals(Boolean.TRUE, summary.get("supported"));
        assertEquals(Boolean.TRUE, ((Map<String, Object>) summary.get("query")).get("byTarget"));
        val counts = (List<Map<String, Object>>) summary.get("counts");
        assertEquals(2L, counts.stream().filter(count -> "mfa-duo".equals(count.get("target")))
            .mapToLong(count -> ((Number) count.get("count")).longValue()).sum());
        assertTrue(counts.stream().allMatch(count -> count.get("target") != null));
    }
}
