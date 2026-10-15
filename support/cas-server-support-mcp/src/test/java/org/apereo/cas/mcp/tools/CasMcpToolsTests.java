package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mcp.BaseCasMcpTests;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.Cookie;
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
@ExtendWith(CasTestExtension.class)
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
    @Qualifier("casAuthenticationEventTools")
    private CasAuthenticationEventTools casAuthenticationEventTools;

    @Autowired
    @Qualifier("casTrustedDeviceTools")
    private CasTrustedDeviceTools casTrustedDeviceTools;

    @Autowired
    @Qualifier("casSurrogateTools")
    private CasSurrogateTools casSurrogateTools;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("casInterruptTools")
    private CasInterruptTools casInterruptTools;

    @Autowired
    @Qualifier("casAuthenticationRiskTools")
    private CasAuthenticationRiskTools casAuthenticationRiskTools;

    @Autowired
    @Qualifier("casPasswordlessTools")
    private CasPasswordlessTools casPasswordlessTools;

    @Autowired
    @Qualifier("casDelegatedAuthenticationTools")
    private CasDelegatedAuthenticationTools casDelegatedAuthenticationTools;

    @Autowired
    @Qualifier("casHeimdallTools")
    private CasHeimdallTools casHeimdallTools;

    @Autowired
    @Qualifier("casSettingResources")
    private CasSettingResources casSettingResources;

    @Autowired
    @Qualifier("casOneTimeTokenTools")
    private CasOneTimeTokenTools casOneTimeTokenTools;

    @Autowired
    @Qualifier("casWebAuthnTools")
    private CasWebAuthnTools casWebAuthnTools;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository oneTimeTokenCredentialRepository;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("casMcpEvaluationContextFactory")
    private CasMcpEvaluationContextFactory casMcpEvaluationContextFactory;

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
        assertTrue(AopUtils.isAopProxy(casAuthenticationEventTools));
        assertTrue(AopUtils.isAopProxy(casTrustedDeviceTools));
        assertTrue(AopUtils.isAopProxy(casSurrogateTools));
        assertTrue(AopUtils.isAopProxy(casInterruptTools));
        assertTrue(AopUtils.isAopProxy(casAuthenticationRiskTools));
        assertTrue(AopUtils.isAopProxy(casOneTimeTokenTools));
        assertTrue(AopUtils.isAopProxy(casWebAuthnTools));
        assertTrue(AopUtils.isAopProxy(casPasswordlessTools));
        assertTrue(AopUtils.isAopProxy(casDelegatedAuthenticationTools));
        assertTrue(AopUtils.isAopProxy(casHeimdallTools));
        assertTrue(AopUtils.isAopProxy(casSettingResources));
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
            () -> casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl, null, null, null));
        val request = new MockHttpServletRequest();
        request.addParameter("authn_method", "something-else");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        try {
            val result = casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl, null, null, null);
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
    void verifyEvaluateMultifactorTriggersForClient() {
        val serviceUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
        val operator = new ClientInfo().setClientIpAddress("203.0.113.99");
        ClientInfoHolder.setClientInfo(operator);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        try {
            val adaptive = casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl, "198.51.100.7", "Firefox", null);
            assertEquals(TestMultifactorAuthenticationProvider.ID, ((Map<?, ?>) adaptive.get("provider")).get("id"), adaptive::toString);
            val activated = ((List<?>) adaptive.get("triggers")).stream()
                .map(trigger -> (Map<?, ?>) trigger)
                .filter(trigger -> "ACTIVATED".equals(trigger.get("outcome")))
                .map(trigger -> String.valueOf(trigger.get("trigger")))
                .toList();
            assertEquals(1, activated.size(), adaptive::toString);
            assertTrue(activated.getFirst().startsWith("AdaptiveMultifactorAuthenticationTrigger"), adaptive::toString);
            assertEquals("Firefox", ((Map<?, ?>) adaptive.get("client")).get("userAgent"));
            assertSame(operator, ClientInfoHolder.getClientInfo());

            val unknown = casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl, " ", null, null);
            assertNull(unknown.get("provider"), unknown::toString);
            assertNull(((Map<?, ?>) unknown.get("client")).get("clientIpAddress"));
            assertSame(operator, ClientInfoHolder.getClientInfo());
        } finally {
            RequestContextHolder.resetRequestAttributes();
            ClientInfoHolder.clear();
        }
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindOneTimeTokenAccounts() {
        val username = "casuser-" + UUID.randomUUID();
        oneTimeTokenCredentialRepository.save(OneTimeTokenAccount.builder()
            .id(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE))
            .username(username)
            .name("Work phone")
            .secretKey("secret-otp-key")
            .validationCode(918273)
            .scratchCodes(List.<Number>of(55667788, 99887766))
            .source("JSON")
            .registrationDate(ZonedDateTime.now(Clock.systemUTC()).minusDays(1))
            .build());
        val result = casOneTimeTokenTools.findOneTimeTokenAccounts(username);
        assertEquals(1, result.get("count"), result::toString);
        val account = (Map<?, ?>) ((List<?>) result.get("accounts")).getFirst();
        assertEquals("Work phone", account.get("name"));
        assertEquals(2, account.get("scratchCodesLeft"));
        assertEquals("JSON", account.get("source"));
        assertNoSecrets(result, "secret-otp-key", "918273", "55667788", "99887766");
        assertEquals(0, casOneTimeTokenTools.findOneTimeTokenAccounts("unknown-" + UUID.randomUUID()).get("count"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindWebAuthnDevices() throws Exception {
        val username = "casuser-" + UUID.randomUUID();
        val credentialId = "ibE9wQddsF806g8uL9hDzgwLJipKhS9esD07Jmj0N98";
        val userHandle = "dXNlci1oYW5kbGUtc2VjcmV0";
        val publicKey = "cHVibGljLWtleS1zZWNyZXQ";
        webAuthnCredentialRepository.addRegistrationByUsername(username, CredentialRegistration.builder()
            .registrationTime(Instant.now(Clock.systemUTC()))
            .credentialNickname("YubiKey 5")
            .transports(new TreeSet<>(Set.of(AuthenticatorTransport.USB)))
            .discoverable(Boolean.TRUE)
            .aaguid("ea9b8d66-4d01-1d21-3ce4-b6b48cb575d4")
            .credential(RegisteredCredential.builder()
                .credentialId(ByteArray.fromBase64Url(credentialId))
                .userHandle(ByteArray.fromBase64Url(userHandle))
                .publicKeyCose(ByteArray.fromBase64Url(publicKey))
                .signatureCount(7)
                .build())
            .userIdentity(UserIdentity.builder()
                .name(username)
                .displayName("CAS")
                .id(ByteArray.fromBase64Url(userHandle))
                .build())
            .build());
        val result = casWebAuthnTools.findWebAuthnDevices(username);
        assertEquals(1, result.get("count"), result::toString);
        val device = (Map<?, ?>) ((List<?>) result.get("devices")).getFirst();
        assertEquals("YubiKey 5", device.get("nickname"));
        assertEquals(List.of("usb"), device.get("transports"));
        assertEquals(Boolean.TRUE, device.get("discoverable"));
        assertEquals(7L, device.get("signatureCount"));
        assertNoSecrets(result, credentialId, userHandle, publicKey);
        assertEquals(0, casWebAuthnTools.findWebAuthnDevices("unknown-" + UUID.randomUUID()).get("count"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindPasswordlessAccount() {
        val username = "passwordless-" + UUID.randomUUID();
        val result = casPasswordlessTools.findPasswordlessAccount(username);
        val account = (Map<?, ?>) result.get("account");
        assertNotNull(account, result::toString);
        assertEquals("j*****@example.org", account.get("email"));
        assertEquals("********23", account.get("phone"));
        assertEquals(Boolean.TRUE, account.get("requestPassword"));
        assertEquals(List.of("Okta"), account.get("allowedDelegatedClients"));
        assertNoSecrets(result, "jsmith@example.org", "3477464523", "attribute-secret");
        assertTrue(casPasswordlessTools.findPasswordlessAccount("casuser").containsKey("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyListIdentityProviders() {
        assertThrows(IllegalStateException.class, () -> casDelegatedAuthenticationTools.listIdentityProviders());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        try {
            val result = casDelegatedAuthenticationTools.listIdentityProviders();
            assertEquals(2, result.get("count"), result::toString);
            val providers = ((List<?>) result.get("identityProviders")).stream().map(provider -> (Map<?, ?>) provider).toList();
            assertEquals(List.of("AnonymousClient", "Okta"), providers.stream().map(provider -> provider.get("name")).toList());
            val okta = providers.getLast();
            assertEquals("Okta Workforce", okta.get("displayName"));
            assertEquals("CLIENT", okta.get("autoRedirectType"));
            assertEquals("https://sso.example.org/cas/login?client_name=Okta", okta.get("callbackUrl"));
            assertTrue(okta.get("type").toString().startsWith("IndirectClient"), okta::toString);
            assertNoSecrets(result, "delegated-client-secret");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindAuthorizableResources() {
        val result = casHeimdallTools.findAuthorizableResources("API");
        assertEquals(1, result.get("count"), result::toString);
        val resource = (Map<?, ?>) ((List<?>) result.get("resources")).getFirst();
        assertEquals("/api/.*", resource.get("pattern"));
        assertEquals("GET", resource.get("method"));
        assertEquals(new TreeSet<>(Set.of("read")), resource.get("actions"));
        assertEquals(List.of("JdbcAuthorizationPolicy"), resource.get("policies"));
        assertEquals(new TreeSet<>(Set.of("owner")), resource.get("propertyNames"));
        assertNoSecrets(result, "jdbc-secret", "jdbc:hsqldb:mem:heimdall", "owner-secret");

        val unknown = casHeimdallTools.findAuthorizableResources("unknown");
        assertTrue(unknown.containsKey("reason"));
        assertEquals(new TreeSet<>(Set.of("API")), unknown.get("knownNamespaces"));
        assertFalse(casHeimdallTools.findAuthorizableResources(null).containsKey("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyDescribeSetting() {
        val setting = casSettingResources.describeSetting("cas.server.name");
        assertTrue(setting.contains("\"module\":\"cas-server-core\""), setting);
        assertTrue(setting.contains("\"defined\":false"), setting);
        assertTrue(setting.contains("\"required\":true"), setting);
        assertTrue(casSettingResources.describeSetting("cas.unknown.setting").contains("No setting is named"));
    }

    private static void assertNoSecrets(final Map<String, Object> result, final String... secrets) {
        val rendered = result.toString();
        for (val secret : secrets) {
            assertFalse(rendered.contains(secret), () -> "Tool result must not contain " + secret + ": " + rendered);
        }
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
    void verifyFindAuthenticationEvents() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        val now = Instant.now(Clock.systemUTC());
        val recent = new CasEvent();
        recent.setType("org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent");
        recent.setPrincipalId(username);
        recent.setCreationTime(now.minus(Duration.ofHours(1)));
        recent.putClientIpAddress("192.0.2.10");
        recent.putAgent("Firefox");
        recent.putDeviceFingerprint("secret-device-fingerprint");
        casEventRepository.save(recent);
        val old = new CasEvent();
        old.setType("org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent");
        old.setPrincipalId(username);
        old.setCreationTime(now.minus(Duration.ofDays(10)));
        casEventRepository.save(old);

        val result = casAuthenticationEventTools.findAuthenticationEvents(username, null);
        assertEquals(1, result.get("count"), result::toString);
        val event = (Map<?, ?>) ((List<?>) result.get("events")).getFirst();
        assertEquals("CasTicketGrantingTicketCreatedEvent", event.get("type"));
        assertEquals("192.0.2.10", event.get("clientIpAddress"));
        assertEquals("Firefox", event.get("userAgent"));
        assertFalse(result.toString().contains("secret-device-fingerprint"));
        assertEquals(2, casAuthenticationEventTools.findAuthenticationEvents(username, 30).get("count"));
    }

    @Test
    void verifyFindAuthenticationEventsWhenEventsAreOff() {
        val repository = BeanSupplier.of(CasEventRepository.class).neverMatch().otherwiseProxy().get();
        val beanFactory = new StaticListableBeanFactory(Map.<String, Object>of(CasEventRepository.BEAN_NAME, repository));
        val tools = new CasAuthenticationEventTools(beanFactory.getBeanProvider(CasEventRepository.class));
        assertEquals("Authentication events are not recorded", tools.findAuthenticationEvents("casuser", null).get("reason"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindTrustedDevices() {
        val username = "casuser-" + UUID.randomUUID();
        val trustRecord = MultifactorAuthenticationTrustRecord.newInstance(username, "Paris", "secret-device-fingerprint");
        trustRecord.setId(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE));
        trustRecord.setRecordKey("secret-record-key");
        trustRecord.setMultifactorAuthenticationProvider(TestMultifactorAuthenticationProvider.ID);
        trustRecord.expireIn(1, ChronoUnit.DAYS);
        mfaTrustEngine.save(trustRecord);

        val result = casTrustedDeviceTools.findTrustedDevices(username);
        assertEquals(1, result.get("count"), result::toString);
        val device = (Map<?, ?>) ((List<?>) result.get("devices")).getFirst();
        assertEquals(trustRecord.getName(), device.get("name"));
        assertEquals(TestMultifactorAuthenticationProvider.ID, device.get("multifactorAuthenticationProvider"));
        assertEquals(Boolean.FALSE, device.get("expired"));
        assertNotNull(device.get("expirationDate"));
        assertFalse(result.toString().contains("secret-device-fingerprint"));
        assertFalse(result.toString().contains("secret-record-key"));
        assertEquals(0, casTrustedDeviceTools.findTrustedDevices("unknown-" + UUID.randomUUID()).get("count"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindSurrogateAccounts() {
        val result = casSurrogateTools.findSurrogateAccounts("casuser", null);
        assertEquals(Boolean.FALSE, result.get("anyAccount"));
        assertEquals(new TreeSet<>(Set.of("banderson", "jsmith")), result.get("accounts"));
        assertFalse(result.containsKey("service"));

        val serviceUrl = newServiceUrl();
        val forService = casSurrogateTools.findSurrogateAccounts("casuser", serviceUrl);
        assertEquals(serviceUrl, forService.get("service"));
        assertEquals(1, forService.get("count"));

        val wildcard = casSurrogateTools.findSurrogateAccounts("surrogate-admin", StringUtils.EMPTY);
        assertEquals(Boolean.TRUE, wildcard.get("anyAccount"));
        assertEquals(new TreeSet<>(Set.of("jsmith")), wildcard.get("accounts"));
    }

    @Test
    void verifyEvaluationContext() {
        val serviceUrl = newServiceUrl();
        val registeredService = newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        servicesManager.save(registeredService);
        val context = casMcpEvaluationContextFactory.build("casuser", serviceUrl + "/login");
        assertEquals(serviceUrl + "/login", context.service().getId());
        assertEquals(registeredService.getId(), Objects.requireNonNull(context.registeredService()).getId());
        assertEquals("casuser", context.principal().getId());
        assertEquals(List.of("casuser@apereo.org"), context.principal().getAttributes().get("mail"));
        assertEquals("casuser", context.authentication().getPrincipal().getId());
        assertEquals("casuser", context.credential().getId());
        assertNull(casMcpEvaluationContextFactory.build("casuser", "https://unknown.example.net/" + UUID.randomUUID()).registeredService());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyPreviewInterrupt() {
        val serviceUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
        val username = "interrupted-" + UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> casInterruptTools.previewInterrupt(username, serviceUrl));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        try {
            val interrupted = casInterruptTools.previewInterrupt(username, serviceUrl);
            val outcomes = ((List<?>) interrupted.get("inquirers")).stream().map(outcome -> (Map<?, ?>) outcome).toList();
            assertEquals(List.of("FailingInquirer", "TermsInquirer"), outcomes.stream().map(outcome -> outcome.get("inquirer")).toList());
            assertEquals(List.of("FAILED", "INTERRUPT"), outcomes.stream().map(outcome -> outcome.get("outcome")).toList());
            assertEquals("Interrupt endpoint is unavailable", outcomes.getFirst().get("error"));
            val interrupt = (Map<?, ?>) interrupted.get("interrupt");
            assertEquals("Accept the terms of " + serviceUrl + " as " + username, interrupt.get("message"));
            assertEquals(Boolean.TRUE, interrupt.get("block"));
            assertEquals(Map.of("Terms", "https://terms.example.org"), interrupt.get("links"));
            assertEquals(Boolean.TRUE, ((Map<?, ?>) interrupted.get("interruptPolicy")).get("enabled"));

            val quiet = casInterruptTools.previewInterrupt("casuser", serviceUrl);
            assertNull(quiet.get("interrupt"));
            assertEquals(List.of("FAILED", "NO_INTERRUPT", "NO_INTERRUPT"),
                ((List<?>) quiet.get("inquirers")).stream().map(outcome -> ((Map<?, ?>) outcome).get("outcome")).toList());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyEvaluateAuthenticationRisk() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        for (var i = 0; i < 2; i++) {
            val event = new CasEvent();
            event.setType(CasTicketGrantingTicketCreatedEvent.class.getName());
            event.setPrincipalId(username);
            event.setCreationTime(Instant.now(Clock.systemUTC()).minus(Duration.ofHours(i + 1)));
            event.putClientIpAddress("192.0.2.10");
            event.putAgent("Firefox");
            casEventRepository.save(event);
        }
        val serviceUrl = newServiceUrl();
        val familiar = casAuthenticationRiskTools.evaluateAuthenticationRisk(username, serviceUrl, "192.0.2.10", "Firefox", null);
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) familiar.get("score")), familiar::toString);
        assertEquals(Boolean.FALSE, familiar.get("risky"));
        assertEquals(0.6, familiar.get("threshold"));
        assertEquals(List.of("IpAddressAuthenticationRequestRiskCalculator", "UserAgentAuthenticationRequestRiskCalculator"),
            ((List<?>) familiar.get("calculators")).stream().map(calculator -> ((Map<?, ?>) calculator).get("calculator")).toList());

        val unfamiliar = casAuthenticationRiskTools.evaluateAuthenticationRisk(username, serviceUrl, "198.51.100.7", "curl", "48.85,2.35");
        assertEquals(0, BigDecimal.ONE.compareTo((BigDecimal) unfamiliar.get("score")), unfamiliar::toString);
        assertEquals(Boolean.TRUE, unfamiliar.get("risky"));
        assertEquals("48.85,2.35", ((Map<?, ?>) unfamiliar.get("client")).get("geoLocation"));

        val unknown = casAuthenticationRiskTools.evaluateAuthenticationRisk("unknown-" + UUID.randomUUID(), serviceUrl, "192.0.2.10", "Firefox", null);
        assertEquals(Boolean.TRUE, unknown.get("risky"));
    }

    @Test
    void verifyHttpExchangeHidesCallerInput() {
        assertThrows(IllegalStateException.class, () -> casMcpEvaluationContextFactory.currentHttpExchange());
        val request = new MockHttpServletRequest();
        request.addParameter("authn_method", "mfa-duo");
        request.addHeader("X-MFA-Provider", "mfa-duo");
        request.setCookies(new Cookie("TGC", "cookie-value"));
        request.setQueryString("authn_method=mfa-duo");
        val response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        try {
            val exchange = casMcpEvaluationContextFactory.currentHttpExchange();
            assertSame(response, exchange.response());
            val stripped = exchange.request();
            assertNull(stripped.getParameter("authn_method"));
            assertTrue(stripped.getParameterMap().isEmpty());
            assertFalse(stripped.getParameterNames().hasMoreElements());
            assertNull(stripped.getParameterValues("authn_method"));
            assertNull(stripped.getHeader("X-MFA-Provider"));
            assertFalse(stripped.getHeaders("X-MFA-Provider").hasMoreElements());
            assertFalse(stripped.getHeaderNames().hasMoreElements());
            assertEquals(-1, stripped.getIntHeader("X-MFA-Provider"));
            assertNull(stripped.getCookies());
            assertNull(stripped.getQueryString());
            assertEquals("unknown", stripped.getRemoteAddr());

            val client = CasMcpEvaluationContextFactory.CasMcpClient.of("198.51.100.7", "Firefox", "48.85,2.35");
            val forClient = casMcpEvaluationContextFactory.currentHttpExchange(client).request();
            assertEquals("Firefox", forClient.getHeader("user-agent"));
            assertEquals(List.of("User-Agent"), Collections.list(forClient.getHeaderNames()));
            assertNull(forClient.getHeader("X-MFA-Provider"));
            assertEquals("48.85,2.35", forClient.getParameter("geolocation"));
            assertNull(forClient.getParameter("authn_method"));
            assertEquals(Set.of("geolocation"), forClient.getParameterMap().keySet());
            assertEquals("198.51.100.7", forClient.getRemoteAddr());
            assertNull(forClient.getCookies());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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
        assertThrows(AccessDeniedException.class,
            () -> casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", "https://mcp.example.org", null, null, null));
        assertThrows(AccessDeniedException.class, () -> casOneTimeTokenTools.findOneTimeTokenAccounts("casuser"));
        assertThrows(AccessDeniedException.class, () -> casWebAuthnTools.findWebAuthnDevices("casuser"));
        assertThrows(AccessDeniedException.class, () -> casPasswordlessTools.findPasswordlessAccount("casuser"));
        assertThrows(AccessDeniedException.class, () -> casDelegatedAuthenticationTools.listIdentityProviders());
        assertThrows(AccessDeniedException.class, () -> casHeimdallTools.findAuthorizableResources("API"));
        assertThrows(AccessDeniedException.class, () -> casSettingResources.describeSetting("cas.server.name"));
        assertThrows(AccessDeniedException.class, () -> casSingleLogoutTools.previewLogout("TGT-1"));
        assertThrows(AccessDeniedException.class, () -> casServerStatisticsTools.getRegistryStatistics());
        assertThrows(AccessDeniedException.class, () -> casTenantTools.findTenant("unknown"));
        assertThrows(AccessDeniedException.class, () -> casAuthenticationEventTools.findAuthenticationEvents("casuser", null));
        assertThrows(AccessDeniedException.class, () -> casTrustedDeviceTools.findTrustedDevices("casuser"));
        assertThrows(AccessDeniedException.class, () -> casSurrogateTools.findSurrogateAccounts("casuser", null));
        assertThrows(AccessDeniedException.class, () -> casInterruptTools.previewInterrupt("casuser", "https://mcp.example.org"));
        assertThrows(AccessDeniedException.class,
            () -> casAuthenticationRiskTools.evaluateAuthenticationRisk("casuser", "https://mcp.example.org", "192.0.2.10", "Firefox", null));
    }

    @Test
    void verifyToolsRequireAuthentication() {
        assertThrows(AuthenticationException.class, () -> casServiceManagementTools.findService(1));
        assertThrows(AuthenticationException.class, () -> casTicketRegistryTools.findTicket("TGT-1"));
        assertThrows(AuthenticationException.class, () -> casPersonDirectoryTools.findPerson("casuser"));
        assertThrows(AuthenticationException.class, () -> casServiceAccessTools.evaluateAccess("casuser", "https://mcp.example.org"));
    }
}
