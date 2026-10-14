package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mcp.BaseCasMcpTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMcpToolsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = BaseCasMcpTests.SharedTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CasMcpToolsTests extends BaseCasMcpTests {
    @Autowired
    @Qualifier("casServiceManagementTools")
    private CasServiceManagementTools casServiceManagementTools;

    @Autowired
    @Qualifier("casServiceAccessTools")
    private CasServiceAccessTools casServiceAccessTools;

    @Autowired
    @Qualifier("casAuditTools")
    private CasAuditTools casAuditTools;

    @Autowired
    @Qualifier("casConsentTools")
    private CasConsentTools casConsentTools;

    @Autowired
    @Qualifier("casMultifactorAuthenticationTools")
    private CasMultifactorAuthenticationTools casMultifactorAuthenticationTools;

    @Autowired
    @Qualifier("casSingleLogoutTools")
    private CasSingleLogoutTools casSingleLogoutTools;

    @Autowired
    @Qualifier("casServerStatisticsTools")
    private CasServerStatisticsTools casServerStatisticsTools;

    @Autowired
    @Qualifier("casTenantTools")
    private CasTenantTools casTenantTools;

    @Autowired
    @Qualifier("casTicketRegistryTools")
    private CasTicketRegistryTools casTicketRegistryTools;

    @Autowired
    @Qualifier("casPersonDirectoryTools")
    private CasPersonDirectoryTools casPersonDirectoryTools;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyToolsAreSecuredByProxies() {
        assertTrue(AopUtils.isAopProxy(casServiceManagementTools));
        assertTrue(AopUtils.isAopProxy(casServiceAccessTools));
        assertTrue(AopUtils.isAopProxy(casAuditTools));
        assertTrue(AopUtils.isAopProxy(casConsentTools));
        assertTrue(AopUtils.isAopProxy(casMultifactorAuthenticationTools));
        assertTrue(AopUtils.isAopProxy(casSingleLogoutTools));
        assertTrue(AopUtils.isAopProxy(casServerStatisticsTools));
        assertTrue(AopUtils.isAopProxy(casTenantTools));
        assertTrue(AopUtils.isAopProxy(casTicketRegistryTools));
        assertTrue(AopUtils.isAopProxy(casPersonDirectoryTools));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindService() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://tools.example.org/" + UUID.randomUUID());
        servicesManager.save(registeredService);
        val result = casServiceManagementTools.findService(registeredService.getId());
        assertNotNull(result);
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val definition = serializer.from(result);
        assertEquals(registeredService.getId(), definition.getId());
        assertEquals(registeredService.getServiceId(), definition.getServiceId());
        assertNull(casServiceManagementTools.findService(-1));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindTicket() throws Throwable {
        val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketId,
            CoreAuthenticationTestUtils.getAuthentication("casuser"), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticketGrantingTicket);
        val result = casTicketRegistryTools.findTicket(ticketId);
        assertNotNull(result);
        val ticket = ticketSerializationManager.deserializeTicket(result, TicketGrantingTicket.class);
        assertEquals(ticketId, ticket.getId());
        assertEquals("casuser", ticket.getAuthentication().getPrincipal().getId());
        assertNull(casTicketRegistryTools.findTicket(UUID.randomUUID().toString()));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindPerson() {
        val username = "casuser-" + UUID.randomUUID();
        val principal = casPersonDirectoryTools.findPerson(username);
        assertNotNull(principal);
        assertEquals(username, principal.getId());
        assertEquals(List.of("casuser@apereo.org"), principal.getAttributes().get("mail"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyMatchService() {
        val serviceUrl = newServiceUrl();
        val registeredService = newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        servicesManager.save(registeredService);
        val result = casServiceManagementTools.matchService(serviceUrl + "/login");
        assertNotNull(result);
        assertEquals(registeredService.getId(), new RegisteredServiceJsonSerializer(applicationContext).from(result).getId());
        assertNull(casServiceManagementTools.matchService("https://unknown.example.net/" + UUID.randomUUID()));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindSessions() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        val ticketIds = new HashSet<String>();
        for (var i = 0; i < 2; i++) {
            val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
                CoreAuthenticationTestUtils.getAuthentication(username), NeverExpiresExpirationPolicy.INSTANCE));
            ticketIds.add(ticketId);
        }
        val result = casTicketRegistryTools.findSessions(username);
        assertEquals(username, result.get("username"));
        assertEquals(2L, result.get("count"));
        val sessions = (List<?>) result.get("sessions");
        assertEquals(ticketIds, sessions.stream().map(session -> ((Map<?, ?>) session).get("id")).collect(Collectors.toSet()));
        assertTrue(((Map<?, ?>) sessions.getFirst()).containsKey("authenticationHandlers"));
        assertEquals(0L, casTicketRegistryTools.findSessions("unknown-" + UUID.randomUUID()).get("count"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyEvaluateAccess() {
        val allowedUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(allowedUrl,
            Map.of("mail", Set.of("casuser@apereo.org")), new ReturnAllAttributeReleasePolicy()));
        val allowed = casServiceAccessTools.evaluateAccess("casuser", allowedUrl);
        assertEquals(Boolean.TRUE, allowed.get("accessAllowed"));
        assertFalse(allowed.containsKey("reason"));

        val disabledUrl = newServiceUrl();
        val disabledService = newRegisteredService(disabledUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        disabledService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        servicesManager.save(disabledService);
        val disabled = casServiceAccessTools.evaluateAccess("casuser", disabledUrl);
        assertEquals(Boolean.FALSE, disabled.get("accessAllowed"));
        assertNotNull(disabled.get("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyPreviewAttributeReleaseForUnknownService() {
        val result = casServiceAccessTools.previewAttributeRelease("casuser", "https://unknown.example.net/" + UUID.randomUUID());
        assertEquals(Boolean.FALSE, result.get("accessAllowed"));
        assertFalse(result.containsKey("releasedAttributes"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifySearchServices() {
        val marker = "tools-search-" + UUID.randomUUID();
        val first = newRegisteredService("https://" + marker + "-1.example.org", Map.of(), new ReturnAllAttributeReleasePolicy());
        val second = newRegisteredService("https://" + marker + "-2.example.org", Map.of(), new ReturnAllAttributeReleasePolicy());
        servicesManager.save(first);
        servicesManager.save(second);
        val result = casServiceManagementTools.searchServices(marker.toUpperCase(Locale.ENGLISH));
        assertEquals(2, result.get("count"));
        val byId = casServiceManagementTools.searchServices(String.valueOf(first.getId()));
        assertTrue(((List<?>) byId.get("services")).stream()
            .anyMatch(service -> Long.valueOf(first.getId()).equals(((Map<?, ?>) service).get("id"))));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindSessionsWithAttribute() throws Throwable {
        val group = "group-" + UUID.randomUUID();
        val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
            CoreAuthenticationTestUtils.getAuthentication("casuser-" + UUID.randomUUID(),
                Map.<String, List<Object>>of("memberOf", List.of(group))), NeverExpiresExpirationPolicy.INSTANCE));
        assertEquals(1, casTicketRegistryTools.findSessionsWithAttribute("memberOf", group).get("count"));
        assertEquals(0, casTicketRegistryTools.findSessionsWithAttribute("memberOf", "missing-" + group).get("count"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindAuditRecordsForUnknownUser() {
        val result = casAuditTools.findAuditRecords("unknown-" + UUID.randomUUID(), null, null);
        assertEquals(0, result.get("count"));
        assertNotNull(result.get("since"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindConsentDecisionsForUnknownUser() {
        val result = casConsentTools.findConsentDecisions("unknown-" + UUID.randomUUID());
        assertEquals(0, result.get("count"));
        assertFalse(result.containsKey("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyEvaluateMultifactorTriggers() {
        val serviceUrl = newServiceUrl();
        val registeredService = newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setMultifactorAuthenticationProviders(Set.of(TestMultifactorAuthenticationProvider.ID));
        registeredService.setMultifactorAuthenticationPolicy(policy);
        servicesManager.save(registeredService);

        assertThrows(IllegalStateException.class,
            () -> casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl));
        val request = new MockHttpServletRequest();
        request.addParameter("authn_method", "something-else");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        try {
            val result = casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl);
            assertEquals(TestMultifactorAuthenticationProvider.ID, ((Map<?, ?>) result.get("provider")).get("id"));
            assertFalse(((List<?>) result.get("triggers")).isEmpty());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        val providers = (List<?>) casMultifactorAuthenticationTools.listMultifactorProviders().get("providers");
        assertTrue(providers.stream().anyMatch(provider -> TestMultifactorAuthenticationProvider.ID.equals(((Map<?, ?>) provider).get("id"))));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyPreviewLogoutAndStatistics() throws Throwable {
        val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
            CoreAuthenticationTestUtils.getAuthentication("casuser-" + UUID.randomUUID()), NeverExpiresExpirationPolicy.INSTANCE));
        val logout = casSingleLogoutTools.previewLogout(ticketId);
        assertEquals(0, logout.get("count"));
        assertEquals(Boolean.FALSE, logout.get("singleLogoutDisabled"));
        assertTrue(casSingleLogoutTools.previewLogout("TGT-" + UUID.randomUUID()).containsKey("reason"));

        val statistics = casServerStatisticsTools.getRegistryStatistics();
        assertTrue(((Number) statistics.get("singleSignOnSessions")).longValue() >= 1);
        assertNotNull(statistics.get("ticketRegistry"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindUnknownTenant() {
        val result = casTenantTools.findTenant("unknown-" + UUID.randomUUID());
        assertTrue(result.containsKey("reason"));
        assertInstanceOf(List.class, result.get("knownTenants"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyToolsDenyUsersWithoutRole() {
        assertThrows(AccessDeniedException.class, () -> casServiceManagementTools.findService(1));
        assertThrows(AccessDeniedException.class, () -> casTicketRegistryTools.findTicket("TGT-1"));
        assertThrows(AccessDeniedException.class, () -> casPersonDirectoryTools.findPerson("casuser"));
        assertThrows(AccessDeniedException.class, () -> casServiceManagementTools.matchService("https://mcp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casTicketRegistryTools.findSessions("casuser"));
        assertThrows(AccessDeniedException.class, () -> casServiceAccessTools.evaluateAccess("casuser", "https://mcp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casServiceAccessTools.previewAttributeRelease("casuser", "https://mcp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casServiceManagementTools.searchServices("mcp"));
        assertThrows(AccessDeniedException.class, () -> casTicketRegistryTools.findSessionsWithAttribute("mail", "casuser@apereo.org"));
        assertThrows(AccessDeniedException.class, () -> casAuditTools.findAuditRecords("casuser", null, null));
        assertThrows(AccessDeniedException.class, () -> casConsentTools.findConsentDecisions("casuser"));
        assertThrows(AccessDeniedException.class, () -> casMultifactorAuthenticationTools.listMultifactorProviders());
        assertThrows(AccessDeniedException.class, () -> casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", "https://mcp.example.org"));
        assertThrows(AccessDeniedException.class, () -> casSingleLogoutTools.previewLogout("TGT-1"));
        assertThrows(AccessDeniedException.class, () -> casServerStatisticsTools.getRegistryStatistics());
        assertThrows(AccessDeniedException.class, () -> casTenantTools.findTenant("unknown"));
    }

    @Test
    void verifyToolsRequireAuthentication() {
        assertThrows(AuthenticationException.class, () -> casServiceManagementTools.findService(1));
        assertThrows(AuthenticationException.class, () -> casTicketRegistryTools.findTicket("TGT-1"));
        assertThrows(AuthenticationException.class, () -> casPersonDirectoryTools.findPerson("casuser"));
        assertThrows(AuthenticationException.class, () -> casServiceAccessTools.evaluateAccess("casuser", "https://mcp.example.org"));
    }
}
