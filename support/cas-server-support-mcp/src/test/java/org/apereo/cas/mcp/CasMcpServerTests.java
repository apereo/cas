package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasMcpServerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
class CasMcpServerTests {
    private static final List<String> CAS_TOOLS = List.of("evaluateAccess", "evaluateMultifactorTriggers",
        "findAuditRecords", "findConsentDecisions", "findPerson", "findService", "findSessions",
        "findSessionsWithAttribute", "findTenant", "findTicket", "getRegistryStatistics", "listMultifactorProviders",
        "matchService", "previewAttributeRelease", "previewLogout", "searchServices",
        "resolveSamlMetadata", "findOidcClient", "listJwksKeys", "findAuthenticationEvents", "findTrustedDevices",
        "findSurrogateAccounts", "previewInterrupt", "evaluateAuthenticationRisk", "findOneTimeTokenAccounts", "findWebAuthnDevices",
        "findPasswordlessAccount", "listIdentityProviders", "findAuthorizableResources", "searchSettings");

    @Nested
    @SpringBootTest(classes = BaseCasMcpTests.SharedTestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class ProtocolTests extends BaseCasMcpTests {
        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        private ServicesManager servicesManager;

        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @Autowired
        @Qualifier("mcpMockAuditTrailManager")
        private MockAuditTrailManager auditTrailManager;

        @Autowired
        @Qualifier(ConsentRepository.BEAN_NAME)
        private ConsentRepository consentRepository;

        @Autowired
        @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
        private ConsentDecisionBuilder consentDecisionBuilder;

        @Autowired
        @Qualifier(TenantsManager.BEAN_NAME)
        private TenantsManager tenantsManager;

        @Autowired
        @Qualifier(CasEventRepository.BEAN_NAME)
        private CasEventRepository casEventRepository;

        @Test
        void verifyEndpointRequiresAuthentication() throws Throwable {
            mockMvc.perform(post(MCP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                    .content(jsonRpcRequest("initialize", "{}")))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyToolsAreListed() throws Throwable {
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN), jsonRpcRequest("tools/list", "{}"));
            val tools = JsonPath.<List<String>>read(response, "$.result.tools[*].name");
            assertTrue(tools.containsAll(CAS_TOOLS), response);
            for (val tool : CAS_TOOLS) {
                val prefix = "$.result.tools[?(@.name == '%s')]".formatted(tool);
                assertEquals(List.of(true), JsonPath.read(response, prefix + ".annotations.readOnlyHint"), response);
                assertEquals(List.of(false), JsonPath.read(response, prefix + ".annotations.destructiveHint"), response);
                assertFalse(JsonPath.<List<String>>read(response, prefix + ".description").getFirst().isBlank());
            }
            val inputSchema = "$.result.tools[?(@.name == 'findService')].inputSchema.properties.serviceId.type";
            assertTrue(JsonPath.<List<String>>read(response, inputSchema).getFirst().matches("integer|number"), response);
        }

        @Test
        void verifyFindService() throws Throwable {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://mcp.example.org/" + UUID.randomUUID());
            servicesManager.save(registeredService);
            val sessionId = initialize(MCP_ADMIN);

            val response = send(MCP_ADMIN, sessionId,
                callToolRequest("findService", "{\"serviceId\":%s}".formatted(registeredService.getId())));
            assertFalse(isToolError(response), response);
            val text = JsonPath.<String>read(response, "$.result.content[0].text");
            assertTrue(text.contains(registeredService.getServiceId()), text);
            assertTrue(text.contains("\"@class\""), text);

            val missing = send(MCP_ADMIN, sessionId, callToolRequest("findService", "{\"serviceId\":-1}"));
            assertEquals("null", JsonPath.read(missing, "$.result.content[0].text"), missing);
        }

        @Test
        void verifyFindTicket() throws Throwable {
            val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
            val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketId,
                CoreAuthenticationTestUtils.getAuthentication("casuser"), NeverExpiresExpirationPolicy.INSTANCE);
            ticketRegistry.addTicket(ticketGrantingTicket);
            val sessionId = initialize(MCP_ADMIN);

            val response = send(MCP_ADMIN, sessionId, callToolRequest("findTicket", "{\"ticketId\":\"%s\"}".formatted(ticketId)));
            assertFalse(isToolError(response), response);
            val text = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(ticketId, JsonPath.read(text, "$.ticketId"), text);
            assertEquals("casuser", JsonPath.read(text, "$.authentication.principal"), text);

            val missing = send(MCP_ADMIN, sessionId,
                callToolRequest("findTicket", "{\"ticketId\":\"%s\"}".formatted(UUID.randomUUID())));
            assertNotNull(JsonPath.read(JsonPath.<String>read(missing, "$.result.content[0].text"), "$.reason"), missing);
        }

        @Test
        void verifyFindPerson() throws Throwable {
            val username = "casuser-" + UUID.randomUUID();
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN),
                callToolRequest("findPerson", "{\"username\":\"%s\"}".formatted(username)));
            assertFalse(isToolError(response), response);
            val text = JsonPath.<String>read(response, "$.result.content[0].text");
            assertTrue(text.contains(username), text);
            assertTrue(text.contains("casuser@apereo.org"), text);
        }

        @Test
        void verifyMatchService() throws Throwable {
            val serviceUrl = newServiceUrl();
            val registeredService = newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
            servicesManager.save(registeredService);
            val sessionId = initialize(MCP_ADMIN);

            val response = send(MCP_ADMIN, sessionId,
                callToolRequest("matchService", "{\"service\":\"%s/app?param=1\"}".formatted(serviceUrl)));
            assertFalse(isToolError(response), response);
            val definition = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(registeredService.getId(), JsonPath.<Number>read(definition, "$.id").longValue(), definition);

            val missing = send(MCP_ADMIN, sessionId,
                callToolRequest("matchService", "{\"service\":\"%s\"}".formatted("https://unknown.example.net/" + UUID.randomUUID())));
            assertEquals("null", JsonPath.read(missing, "$.result.content[0].text"), missing);
        }

        @Test
        void verifyFindSessions() throws Throwable {
            val username = "casuser-" + UUID.randomUUID();
            val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
                CoreAuthenticationTestUtils.getAuthentication(username), NeverExpiresExpirationPolicy.INSTANCE));

            val response = send(MCP_ADMIN, initialize(MCP_ADMIN),
                callToolRequest("findSessions", "{\"username\":\"%s\"}".formatted(username)));
            assertFalse(isToolError(response), response);
            val sessions = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(1, JsonPath.<Number>read(sessions, "$.count").intValue(), sessions);
            assertEquals(List.of(CasMcpSessionReferences.of(ticketId)), JsonPath.read(sessions, "$.sessions[*].sessionReference"), sessions);
            assertFalse(sessions.contains(ticketId), sessions);
        }

        @Test
        void verifyEvaluateAccess() throws Throwable {
            val allowedUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(allowedUrl,
                Map.of("mail", Set.of("casuser@apereo.org")), new ReturnAllAttributeReleasePolicy()));
            val deniedUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(deniedUrl,
                Map.of("mail", Set.of("someone-else@apereo.org")), new ReturnAllAttributeReleasePolicy()));
            val sessionId = initialize(MCP_ADMIN);

            val allowed = toolResult(sessionId, "evaluateAccess", allowedUrl);
            assertTrue(JsonPath.<Boolean>read(allowed, "$.accessAllowed"), allowed);

            val denied = toolResult(sessionId, "evaluateAccess", deniedUrl);
            assertFalse(JsonPath.<Boolean>read(denied, "$.accessAllowed"), denied);
            assertFalse(JsonPath.<String>read(denied, "$.reason").isBlank(), denied);

            val unregistered = toolResult(sessionId, "evaluateAccess", "https://unknown.example.net/" + UUID.randomUUID());
            assertFalse(JsonPath.<Boolean>read(unregistered, "$.accessAllowed"), unregistered);
            assertTrue(JsonPath.<String>read(unregistered, "$.reason").startsWith("No registered service"), unregistered);
        }

        @Test
        void verifyPreviewAttributeRelease() throws Throwable {
            val releaseAllUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(releaseAllUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
            val releaseNoneUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(releaseNoneUrl, Map.of(), new ReturnAllowedAttributeReleasePolicy(List.of("cn"))));
            val sessionId = initialize(MCP_ADMIN);

            val releaseAll = toolResult(sessionId, "previewAttributeRelease", releaseAllUrl);
            assertEquals("casuser", JsonPath.read(releaseAll, "$.releasedUsername"), releaseAll);
            assertEquals(List.of("casuser@apereo.org"), JsonPath.read(releaseAll, "$.releasedAttributes.mail"), releaseAll);

            val releaseNone = toolResult(sessionId, "previewAttributeRelease", releaseNoneUrl);
            assertFalse(JsonPath.<Map<String, Object>>read(releaseNone, "$.releasedAttributes").containsKey("mail"), releaseNone);
        }

        @Test
        void verifySearchServices() throws Throwable {
            val marker = "search-" + UUID.randomUUID();
            val registeredService = newRegisteredService("https://" + marker + ".example.org", Map.of(), new ReturnAllAttributeReleasePolicy());
            servicesManager.save(registeredService);
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN), callToolRequest("searchServices", "{\"query\":\"%s\"}".formatted(marker)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(1, JsonPath.<Number>read(result, "$.count").intValue(), result);
            assertEquals(registeredService.getId(), JsonPath.<Number>read(result, "$.services[0].id").longValue(), result);
        }

        @Test
        void verifyFindSessionsWithAttribute() throws Throwable {
            val department = "department-" + UUID.randomUUID();
            val username = "casuser-" + UUID.randomUUID();
            val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
                CoreAuthenticationTestUtils.getAuthentication(username, Map.<String, List<Object>>of("department", List.of(department))),
                NeverExpiresExpirationPolicy.INSTANCE));
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN), callToolRequest("findSessionsWithAttribute",
                "{\"attributeName\":\"department\",\"attributeValue\":\"%s\"}".formatted(department)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(List.of(CasMcpSessionReferences.of(ticketId)), JsonPath.read(result, "$.sessions[*].sessionReference"), result);
            assertFalse(result.contains(ticketId), result);
            assertEquals(List.of(username), JsonPath.read(result, "$.sessions[*].principal"), result);
        }

        @Test
        void verifyFindAuthenticationEvents() throws Throwable {
            val username = "casuser-" + UUID.randomUUID();
            val event = new CasEvent();
            event.setType("org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent");
            event.setPrincipalId(username);
            event.setCreationTime(Instant.now(Clock.systemUTC()));
            event.putClientIpAddress("192.0.2.10");
            event.putGeoAddress("Paris");
            event.putDeviceFingerprint("secret-device-fingerprint");
            casEventRepository.save(event);
            val sessionId = initialize(MCP_ADMIN);

            val response = send(MCP_ADMIN, sessionId,
                callToolRequest("findAuthenticationEvents", "{\"username\":\"%s\"}".formatted(username)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(List.of("CasTicketGrantingTicketCreatedEvent"), JsonPath.read(result, "$.events[*].type"), result);
            assertEquals("Paris", JsonPath.read(result, "$.events[0].geoAddress"), result);
            assertFalse(result.contains("secret-device-fingerprint"), result);
        }

        @Test
        void verifyPreviewInterrupt() throws Throwable {
            val serviceUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
            val username = "interrupted-" + UUID.randomUUID();
            val sessionId = initialize(MCP_ADMIN);
            val response = send(MCP_ADMIN, sessionId, callToolRequest("previewInterrupt",
                "{\"username\":\"%s\",\"service\":\"%s\"}".formatted(username, serviceUrl)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals("Accept the terms of %s as %s".formatted(serviceUrl, username), JsonPath.read(result, "$.interrupt.message"), result);
            assertEquals(List.of("FAILED", "INTERRUPT"), JsonPath.read(result, "$.inquirers[*].outcome"), result);
            val definition = send(MCP_ADMIN, sessionId, jsonRpcRequest("tools/list", "{}"));
            assertEquals(List.of(true), JsonPath.read(definition, "$.result.tools[?(@.name == 'previewInterrupt')].annotations.openWorldHint"));
        }

        @Test
        void verifySettingResource() throws Throwable {
            mockMvc.perform(post(MCP_ENDPOINT)
                    .with(MCP_ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                    .content(jsonRpcRequest("initialize", """
                        {"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"cas-tests","version":"1.0.0"}}
                        """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.capabilities.resources").exists());
            val sessionId = initialize(MCP_ADMIN);
            val templates = send(MCP_ADMIN, sessionId, jsonRpcRequest("resources/templates/list", "{}"));
            assertEquals(List.of("cas://settings/{name}"), JsonPath.read(templates, "$.result.resourceTemplates[*].uriTemplate"), templates);
            assertEquals(List.of("application/json"), JsonPath.read(templates, "$.result.resourceTemplates[*].mimeType"), templates);

            val known = send(MCP_ADMIN, sessionId, jsonRpcRequest("resources/read", "{\"uri\":\"cas://settings/cas.server.name\"}"));
            assertEquals("cas://settings/cas.server.name", JsonPath.read(known, "$.result.contents[0].uri"), known);
            val setting = JsonPath.<String>read(known, "$.result.contents[0].text");
            assertEquals("cas-server-core", JsonPath.read(setting, "$.setting.module"), setting);
            assertEquals(Boolean.TRUE, JsonPath.read(setting, "$.setting.required"), setting);
            assertEquals("https://cas.example.org:8443", JsonPath.read(setting, "$.setting.defaultValue"), setting);
            assertEquals(Boolean.FALSE, JsonPath.read(setting, "$.defined"), setting);

            val defined = send(MCP_ADMIN, sessionId, jsonRpcRequest("resources/read", "{\"uri\":\"cas://settings/spring.ai.mcp.server.name\"}"));
            val definedSetting = JsonPath.<String>read(defined, "$.result.contents[0].text");
            assertEquals(Boolean.TRUE, JsonPath.read(definedSetting, "$.defined"), definedSetting);
            assertFalse(definedSetting.contains("Apereo CAS MCP Server"), definedSetting);

            val unknown = send(MCP_ADMIN, sessionId, jsonRpcRequest("resources/read", "{\"uri\":\"cas://settings/cas.unknown.setting\"}"));
            val unknownSetting = JsonPath.<String>read(unknown, "$.result.contents[0].text");
            assertEquals("No setting is named cas.unknown.setting", JsonPath.read(unknownSetting, "$.reason"), unknownSetting);
        }

        @Test
        void verifyFindAuditRecords() throws Throwable {
            val username = "casuser-" + UUID.randomUUID();
            val clientInfo = new ClientInfo("192.0.2.10", "192.0.2.1", "Firefox", "Earth");
            auditTrailManager.record(new AuditActionContext(username, "https://app.example.org",
                "AUTHENTICATION_FAILED", "CAS", LocalDateTime.now(Clock.systemUTC()).minusHours(1), clientInfo));
            auditTrailManager.record(new AuditActionContext(username, "https://app.example.org",
                "AUTHENTICATION_SUCCESS", "CAS", LocalDateTime.now(Clock.systemUTC()), clientInfo));
            auditTrailManager.record(new AuditActionContext(username, "https://app.example.org",
                "AUTHENTICATION_SUCCESS", "CAS", LocalDateTime.now(Clock.systemUTC()).minusDays(30), clientInfo));
            val sessionId = initialize(MCP_ADMIN);

            val all = send(MCP_ADMIN, sessionId, callToolRequest("findAuditRecords", "{\"username\":\"%s\"}".formatted(username)));
            assertFalse(isToolError(all), all);
            val records = JsonPath.<String>read(all, "$.result.content[0].text");
            assertEquals(List.of("AUTHENTICATION_SUCCESS", "AUTHENTICATION_FAILED"), JsonPath.read(records, "$.records[*].action"), records);
            assertEquals("192.0.2.10", JsonPath.read(records, "$.records[0].clientIpAddress"), records);

            val failures = send(MCP_ADMIN, sessionId, callToolRequest("findAuditRecords",
                "{\"username\":\"%s\",\"days\":60,\"action\":\"failed\"}".formatted(username)));
            val failed = JsonPath.<String>read(failures, "$.result.content[0].text");
            assertEquals(List.of("AUTHENTICATION_FAILED"), JsonPath.read(failed, "$.records[*].action"), failed);
        }

        @Test
        void verifyFindConsentDecisions() throws Throwable {
            val username = "casuser-" + UUID.randomUUID();
            val serviceUrl = newServiceUrl();
            val decision = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(serviceUrl),
                newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy()),
                username, Map.of("mail", List.of("casuser@apereo.org"), "cn", List.of("CAS")));
            consentRepository.storeConsentDecision(decision);

            val response = send(MCP_ADMIN, initialize(MCP_ADMIN),
                callToolRequest("findConsentDecisions", "{\"username\":\"%s\"}".formatted(username)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(1, JsonPath.<Number>read(result, "$.count").intValue(), result);
            assertEquals(serviceUrl, JsonPath.read(result, "$.decisions[0].service"), result);
            assertEquals(List.of("cn", "mail"), JsonPath.read(result, "$.decisions[0].attributes"), result);
            assertFalse(result.contains("casuser@apereo.org"), result);
        }

        @Test
        void verifyMultifactorProviders() throws Throwable {
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN), callToolRequest("listMultifactorProviders", "{}"));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertTrue(JsonPath.<List<String>>read(result, "$.providers[*].id").contains(TestMultifactorAuthenticationProvider.ID), result);
        }

        @Test
        void verifyEvaluateMultifactorTriggers() throws Throwable {
            val mfaUrl = newServiceUrl();
            val mfaService = newRegisteredService(mfaUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setMultifactorAuthenticationProviders(Set.of(TestMultifactorAuthenticationProvider.ID));
            mfaService.setMultifactorAuthenticationPolicy(policy);
            servicesManager.save(mfaService);

            val bypassUrl = newServiceUrl();
            val bypassService = newRegisteredService(bypassUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
            val bypassPolicy = new DefaultRegisteredServiceMultifactorPolicy();
            bypassPolicy.setMultifactorAuthenticationProviders(Set.of(TestMultifactorAuthenticationProvider.ID));
            bypassPolicy.setBypassEnabled(true);
            bypassService.setMultifactorAuthenticationPolicy(bypassPolicy);
            servicesManager.save(bypassService);

            val plainUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(plainUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
            val sessionId = initialize(MCP_ADMIN);

            val required = toolResult(sessionId, "evaluateMultifactorTriggers", mfaUrl);
            assertEquals(TestMultifactorAuthenticationProvider.ID, JsonPath.read(required, "$.provider.id"), required);
            assertEquals(List.of("ACTIVATED"), JsonPath.read(required, "$.triggers[?(@.outcome == 'ACTIVATED')].outcome"), required);

            val bypassed = toolResult(sessionId, "evaluateMultifactorTriggers", bypassUrl);
            assertTrue(JsonPath.<Boolean>read(bypassed, "$.bypassedByService"), bypassed);
            assertNull(JsonPath.read(bypassed, "$.provider"), bypassed);

            val notRequired = toolResult(sessionId, "evaluateMultifactorTriggers", plainUrl);
            assertNull(JsonPath.read(notRequired, "$.provider"), notRequired);
            assertTrue(JsonPath.<List<String>>read(notRequired, "$.triggers[*].outcome").stream()
                .noneMatch("ACTIVATED"::equals), notRequired);
            assertNull(JsonPath.read(notRequired, "$.client.clientIpAddress"), notRequired);

            val adaptive = send(MCP_ADMIN, sessionId, callToolRequest("evaluateMultifactorTriggers",
                "{\"username\":\"casuser\",\"service\":\"%s\",\"clientIpAddress\":\"198.51.100.7\"}".formatted(plainUrl)));
            assertFalse(isToolError(adaptive), adaptive);
            val adaptiveResult = JsonPath.<String>read(adaptive, "$.result.content[0].text");
            assertEquals(TestMultifactorAuthenticationProvider.ID, JsonPath.read(adaptiveResult, "$.provider.id"), adaptiveResult);
            assertEquals("198.51.100.7", JsonPath.read(adaptiveResult, "$.client.clientIpAddress"), adaptiveResult);
        }

        @Test
        void verifyPreviewLogout() throws Throwable {
            val backChannelUrl = newServiceUrl();
            servicesManager.save(newRegisteredService(backChannelUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
            val silentUrl = newServiceUrl();
            val silentService = newRegisteredService(silentUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
            silentService.setLogoutType(RegisteredServiceLogoutType.NONE);
            servicesManager.save(silentService);

            val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
            val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketId,
                CoreAuthenticationTestUtils.getAuthentication("casuser-" + UUID.randomUUID()), NeverExpiresExpirationPolicy.INSTANCE);
            ticketGrantingTicket.getServices().put("ST-1-" + UUID.randomUUID(), RegisteredServiceTestUtils.getService(backChannelUrl));
            ticketGrantingTicket.getServices().put("ST-2-" + UUID.randomUUID(), RegisteredServiceTestUtils.getService(silentUrl));
            ticketGrantingTicket.getServices().put("ST-3-" + UUID.randomUUID(),
                RegisteredServiceTestUtils.getService("https://unknown.example.net/" + UUID.randomUUID()));
            ticketRegistry.addTicket(ticketGrantingTicket);
            val sessionId = initialize(MCP_ADMIN);

            val response = send(MCP_ADMIN, sessionId, callToolRequest("previewLogout", "{\"ticketId\":\"%s\"}".formatted(ticketId)));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertEquals(3, JsonPath.<Number>read(result, "$.count").intValue(), result);
            assertEquals(List.of(true), JsonPath.read(result, "$.applications[?(@.service == '%s')].notified".formatted(backChannelUrl)), result);
            assertEquals(List.of(false), JsonPath.read(result, "$.applications[?(@.service == '%s')].notified".formatted(silentUrl)), result);
            assertEquals(2, JsonPath.<List<Boolean>>read(result, "$.applications[?(@.notified == false)].notified").size(), result);

            val missing = send(MCP_ADMIN, sessionId, callToolRequest("previewLogout", "{\"ticketId\":\"TGT-unknown\"}"));
            assertTrue(JsonPath.<String>read(JsonPath.<String>read(missing, "$.result.content[0].text"), "$.reason")
                .startsWith("No active single sign-on session"), missing);
        }

        @Test
        void verifyRegistryStatistics() throws Throwable {
            servicesManager.save(newRegisteredService(newServiceUrl(), Map.of(), new ReturnAllAttributeReleasePolicy()));
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX),
                CoreAuthenticationTestUtils.getAuthentication("casuser-" + UUID.randomUUID()), NeverExpiresExpirationPolicy.INSTANCE));
            val response = send(MCP_ADMIN, initialize(MCP_ADMIN), callToolRequest("getRegistryStatistics", "{}"));
            assertFalse(isToolError(response), response);
            val result = JsonPath.<String>read(response, "$.result.content[0].text");
            assertTrue(JsonPath.<Number>read(result, "$.registeredServices").longValue() >= 1, result);
            assertTrue(JsonPath.<Number>read(result, "$.singleSignOnSessions").longValue() >= 1, result);
            assertTrue(JsonPath.<Number>read(result, "$.tickets").longValue() >= 1, result);
        }

        @Test
        void verifyFindTenant() throws Throwable {
            val tenant = new TenantDefinition();
            tenant.setId("tenant-" + UUID.randomUUID());
            tenant.setDescription("MCP tenant");
            tenant.setProperties(Map.<String, Object>of("cas.tgc.crypto.signing.key", "super-secret-signing-key"));
            tenantsManager.save(tenant);
            val sessionId = initialize(MCP_ADMIN);

            val found = send(MCP_ADMIN, sessionId, callToolRequest("findTenant", "{\"tenantId\":\"%s\"}".formatted(tenant.getId())));
            assertFalse(isToolError(found), found);
            val result = JsonPath.<String>read(found, "$.result.content[0].text");
            assertEquals("MCP tenant", JsonPath.read(result, "$.tenant.description"), result);
            assertEquals(List.of("cas.tgc.crypto.signing.key"), JsonPath.read(result, "$.tenant.overriddenSettings"), result);
            assertFalse(result.contains("super-secret-signing-key"), result);

            val missing = send(MCP_ADMIN, sessionId, callToolRequest("findTenant", "{\"tenantId\":\"unknown-tenant\"}"));
            val unknown = JsonPath.<String>read(missing, "$.result.content[0].text");
            assertTrue(JsonPath.<List<String>>read(unknown, "$.knownTenants").contains(tenant.getId()), unknown);
        }

        private String toolResult(final String sessionId, final String tool, final String serviceUrl) throws Exception {
            val response = send(MCP_ADMIN, sessionId,
                callToolRequest(tool, "{\"username\":\"casuser\",\"service\":\"%s\"}".formatted(serviceUrl)));
            assertFalse(isToolError(response), response);
            return JsonPath.read(response, "$.result.content[0].text");
        }

        @Test
        void verifyToolsRequireRole() throws Throwable {
            val sessionId = initialize(CAS_USER);
            assertTrue(listTools(CAS_USER).containsAll(CAS_TOOLS));
            val calls = List.of(
                callToolRequest("findService", "{\"serviceId\":1}"),
                callToolRequest("matchService", "{\"service\":\"https://mcp.example.org\"}"),
                callToolRequest("findTicket", "{\"ticketId\":\"TGT-1\"}"),
                callToolRequest("findSessions", "{\"username\":\"casuser\"}"),
                callToolRequest("findPerson", "{\"username\":\"casuser\"}"),
                callToolRequest("evaluateAccess", "{\"username\":\"casuser\",\"service\":\"https://mcp.example.org\"}"),
                callToolRequest("previewAttributeRelease", "{\"username\":\"casuser\",\"service\":\"https://mcp.example.org\"}"),
                callToolRequest("searchServices", "{\"query\":\"mcp\"}"),
                callToolRequest("findSessionsWithAttribute", "{\"attributeName\":\"mail\",\"attributeValue\":\"casuser@apereo.org\"}"),
                callToolRequest("findAuditRecords", "{\"username\":\"casuser\"}"),
                callToolRequest("findConsentDecisions", "{\"username\":\"casuser\"}"),
                callToolRequest("listMultifactorProviders", "{}"),
                callToolRequest("evaluateMultifactorTriggers", "{\"username\":\"casuser\",\"service\":\"https://mcp.example.org\"}"),
                callToolRequest("previewLogout", "{\"ticketId\":\"TGT-1\"}"),
                callToolRequest("getRegistryStatistics", "{}"),
                callToolRequest("findTenant", "{\"tenantId\":\"unknown\"}"),
                callToolRequest("findAuthenticationEvents", "{\"username\":\"casuser\"}"),
                callToolRequest("findTrustedDevices", "{\"username\":\"casuser\"}"),
                callToolRequest("findSurrogateAccounts", "{\"username\":\"casuser\"}"),
                callToolRequest("findOneTimeTokenAccounts", "{\"username\":\"casuser\"}"),
                callToolRequest("findWebAuthnDevices", "{\"username\":\"casuser\"}"),
                callToolRequest("findPasswordlessAccount", "{\"username\":\"casuser\"}"),
                callToolRequest("listIdentityProviders", "{}"),
                callToolRequest("findAuthorizableResources", "{\"namespace\":\"API\"}"),
                callToolRequest("searchSettings", "{\"query\":\"server\"}"),
                callToolRequest("previewInterrupt", "{\"username\":\"casuser\",\"service\":\"https://mcp.example.org\"}"),
                callToolRequest("evaluateAuthenticationRisk",
                    "{\"username\":\"casuser\",\"service\":\"https://mcp.example.org\",\"clientIpAddress\":\"192.0.2.10\",\"userAgent\":\"Firefox\"}"));
            for (val call : calls) {
                val response = send(CAS_USER, sessionId, call);
                assertTrue(isToolError(response), response);
            }
        }
    }
    
    @Nested
    @SpringBootTest(classes = BaseCasMcpTests.SharedTestConfiguration.class,
        properties = "CasFeatureModule.AI.mcp.enabled=false",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class FeatureDisabledTests extends BaseCasMcpTests {
        @Test
        void verifyToolsAreNotRegistered() {
            assertFalse(webApplicationContext.containsBean("casServiceManagementTools"));
            assertFalse(webApplicationContext.containsBean("casTicketRegistryTools"));
            assertFalse(webApplicationContext.containsBean("casPersonDirectoryTools"));
            assertFalse(webApplicationContext.containsBean("casServiceAccessTools"));
            assertFalse(webApplicationContext.containsBean("casAuditTools"));
            assertFalse(webApplicationContext.containsBean("casConsentTools"));
            assertFalse(webApplicationContext.containsBean("casMultifactorAuthenticationTools"));
            assertFalse(webApplicationContext.containsBean("casSingleLogoutTools"));
            assertFalse(webApplicationContext.containsBean("casServerStatisticsTools"));
            assertFalse(webApplicationContext.containsBean("casTenantTools"));
            assertFalse(webApplicationContext.containsBean("casSamlIdPMetadataTools"));
            assertFalse(webApplicationContext.containsBean("casOidcTools"));
            assertFalse(webApplicationContext.containsBean("casAuthenticationEventTools"));
            assertFalse(webApplicationContext.containsBean("casTrustedDeviceTools"));
            assertFalse(webApplicationContext.containsBean("casSurrogateTools"));
            assertFalse(webApplicationContext.containsBean("casInterruptTools"));
            assertFalse(webApplicationContext.containsBean("casOneTimeTokenTools"));
            assertFalse(webApplicationContext.containsBean("casWebAuthnTools"));
            assertFalse(webApplicationContext.containsBean("casPasswordlessTools"));
            assertFalse(webApplicationContext.containsBean("casDelegatedAuthenticationTools"));
            assertFalse(webApplicationContext.containsBean("casHeimdallTools"));
            assertFalse(webApplicationContext.containsBean("casSettingResources"));
            assertFalse(webApplicationContext.containsBean("casSettingTools"));
            assertFalse(webApplicationContext.containsBean("casMcpAuditAdvisor"));
            assertFalse(webApplicationContext.containsBean("casAuthenticationRiskTools"));
            assertFalse(webApplicationContext.containsBean("casMcpEvaluationContextFactory"));
            assertFalse(webApplicationContext.containsBean("casMcpEndpointConfigurer"));
        }
    }
}
