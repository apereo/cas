package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mcp.BaseCasMcpTests;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.CasMcpSessionReferences;
import org.apereo.cas.mcp.audit.CasMcpAuditor;
import org.apereo.cas.mcp.audit.CasMcpOperation;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditActionResolver;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditResourceResolver;
import org.apereo.cas.mcp.resources.CasServiceResources;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
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
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.text.MessageSanitizer;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.jayway.jsonpath.JsonPath;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.aspectj.lang.JoinPoint;
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
import static org.mockito.Mockito.*;

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
    @Qualifier("casSettingTools")
    private CasSettingTools casSettingTools;

    @Autowired
    @Qualifier("casMcpAuditor")
    private BaseCasMcpTests.RecordingCasMcpAuditor casMcpAuditor;

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
    @Qualifier("casDeploymentTools")
    private CasDeploymentTools casDeploymentTools;

    @Autowired
    @Qualifier("casThrottleTools")
    private CasThrottleTools casThrottleTools;

    @Autowired
    @Qualifier("casAttributeTools")
    private CasAttributeTools casAttributeTools;

    @Autowired
    @Qualifier("casLoginExplanationTools")
    private CasLoginExplanationTools casLoginExplanationTools;

    @Autowired
    @Qualifier("casServiceResources")
    private CasServiceResources casServiceResources;

    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttledSubmissionsStore;

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;

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
        assertTrue(AopUtils.isAopProxy(casSettingTools));
        assertTrue(AopUtils.isAopProxy(casDeploymentTools));
        assertTrue(AopUtils.isAopProxy(casThrottleTools));
        assertTrue(AopUtils.isAopProxy(casAttributeTools));
        assertTrue(AopUtils.isAopProxy(casLoginExplanationTools));
        assertTrue(AopUtils.isAopProxy(casServiceResources));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyDescribeDeployment() {
        val deployment = casDeploymentTools.describeDeployment();
        assertFalse(deployment.casVersion().isBlank(), deployment::toString);
        assertFalse(deployment.serverName().isBlank());
        assertFalse(deployment.ticketRegistry().isBlank());
        assertTrue(deployment.features().contains("AI.mcp"), deployment::toString);
        assertFalse(deployment.attributeRepositories().isEmpty(), deployment::toString);
        assertTrue(deployment.mcpToolGroups().stream()
            .anyMatch(group -> "mcp-consent".equals(group.feature()) && group.active()
                && "CasFeatureModule.AI.mcp-consent.enabled".equals(group.property())), deployment::toString);
        assertEquals(14, deployment.mcpToolGroups().size());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindThrottledLogins() {
        val username = "throttled-" + UUID.randomUUID();
        val now = ZonedDateTime.now(Clock.systemUTC());
        throttledSubmissionsStore.put(ThrottledSubmission.builder()
            .id(UUID.randomUUID().toString())
            .key("192.0.2.20;" + username)
            .username(username)
            .clientIpAddress("192.0.2.20")
            .value(now.minusSeconds(30))
            .expiration(now.plusMinutes(5))
            .build());
        val otherAddress = "192.0.2." + ThreadLocalRandom.current().nextInt(100, 200);
        throttledSubmissionsStore.put(ThrottledSubmission.builder()
            .id(UUID.randomUUID().toString())
            .key(otherAddress + ";someone-" + username)
            .username("someone-" + username)
            .clientIpAddress(otherAddress)
            .value(now.minusHours(1))
            .build());

        val result = casThrottleTools.findThrottledLogins(username.toUpperCase(Locale.ENGLISH), null);
        assertEquals(1, result.count(), result::toString);
        assertNull(result.reason());
        assertFalse(result.auditBased());
        assertEquals(3, result.settings().failureThreshold());
        val login = result.logins().getFirst();
        assertEquals(username, login.username());
        assertTrue(login.throttled());
        assertNotNull(login.lockedUntil());

        val byAddress = casThrottleTools.findThrottledLogins(null, otherAddress);
        assertEquals(1, byAddress.count(), byAddress::toString);
        assertFalse(byAddress.logins().getFirst().throttled());
        assertNull(byAddress.logins().getFirst().lockedUntil());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindAttributeSourcesAndDefinitions() {
        val sources = casAttributeTools.findAttributeSources("casuser");
        assertNull(sources.reason());
        assertTrue(sources.repositories().stream()
            .anyMatch(source -> source.found() && source.attributeNames().contains("mail")), sources::toString);
        assertNoSecrets(sources, "casuser@apereo.org");

        val key = "mcpEmployeeId" + UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);
        attributeDefinitionStore.registerAttributeDefinition(DefaultAttributeDefinition.builder()
            .key(key)
            .name("urn:oid:" + key)
            .attribute("uid")
            .scoped(true)
            .script("groovy { return ['definition-secret'] }")
            .build());
        val definitions = casAttributeTools.findAttributeDefinitions(key.toUpperCase(Locale.ENGLISH));
        assertEquals(1, definitions.count(), definitions::toString);
        val definition = definitions.definitions().getFirst();
        assertEquals("urn:oid:" + key, definition.name());
        assertEquals("uid", definition.attribute());
        assertTrue(definition.scoped());
        assertTrue(definition.scripted());
        assertNoSecrets(definitions, "definition-secret");
        assertEquals(0, casAttributeTools.findAttributeDefinitions("no-such-definition-" + UUID.randomUUID()).count());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyValidateServiceDefinition() {
        val serviceUrl = newServiceUrl();
        val existing = newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        existing.setEvaluationOrder(100);
        servicesManager.save(existing);
        val candidateId = ThreadLocalRandom.current().nextLong(Integer.MAX_VALUE, Long.MAX_VALUE);
        val definition = """
            {
              "@class": "org.apereo.cas.services.CasRegisteredService",
              "serviceId": "%s/app.*",
              "name": "MCP-Candidate-%s",
              "id": %s,
              "evaluationOrder": 1
            }
            """.formatted(serviceUrl, candidateId, candidateId);
        val result = casServiceManagementTools.validateServiceDefinition(definition, List.of(serviceUrl + "/app/login", serviceUrl + "/other"));
        assertTrue(result.valid(), result::toString);
        assertEquals(candidateId, Objects.requireNonNull(result.service()).id());
        val app = result.urls().getFirst();
        assertTrue(app.matches());
        assertEquals(existing.getId(), Objects.requireNonNull(app.currentMatch()).id());
        assertEquals(CasServiceManagementTools.Winner.CANDIDATE, app.winner());
        val other = result.urls().getLast();
        assertFalse(other.matches());
        assertEquals(CasServiceManagementTools.Winner.EXISTING, other.winner());

        val replacing = casServiceManagementTools.validateServiceDefinition("""
            {"@class":"org.apereo.cas.services.CasRegisteredService","serviceId":"%s.*","name":"%s","id":%s}
            """.formatted(serviceUrl, existing.getName(), existing.getId()), null);
        assertTrue(replacing.warnings().stream().anyMatch(warning -> warning.startsWith("Saving would replace")), replacing::toString);
        assertTrue(replacing.warnings().stream().anyMatch(warning -> warning.startsWith("No application URLs")), replacing::toString);

        val unreadable = casServiceManagementTools.validateServiceDefinition("{\"serviceId\":", null);
        assertFalse(unreadable.valid());
        assertTrue(unreadable.errors().getFirst().startsWith("CAS cannot read"), unreadable::toString);

        val badPattern = casServiceManagementTools.validateServiceDefinition("""
            {"@class":"org.apereo.cas.services.CasRegisteredService","serviceId":"https://(unclosed","name":"Broken","id":5}
            """, null);
        assertFalse(badPattern.valid(), badPattern::toString);
    }

    @Test
    @WithMockUser(username = "operator", roles = "MCP_ADMIN")
    void verifyExplainLogin() {
        val serviceUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(serviceUrl, Map.of(), new ReturnAllAttributeReleasePolicy()));
        val deniedUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(deniedUrl, Map.of("mail", Set.of("nobody@apereo.org")), new ReturnAllAttributeReleasePolicy()));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        try {
            val explanation = casLoginExplanationTools.explainLogin("casuser", serviceUrl, null, null, null);
            assertEquals(CasLoginExplanationTools.Outcome.LOGIN_SUCCEEDS, explanation.outcome(), explanation::toString);
            assertEquals(List.of("access", "risk", "multifactor", "interrupt", "attributeRelease"),
                explanation.steps().stream().map(CasLoginExplanationTools.LoginStep::step).toList());
            assertEquals(CasLoginExplanationTools.StepStatus.SKIPPED, explanation.steps().get(1).status());
            assertEquals("casuser", explanation.steps().getLast().result().get("releasedUsername"));
            assertTrue(casMcpAuditor.getOperations().stream()
                .filter(operation -> serviceUrl.equals(operation.arguments().get("service")))
                .allMatch(operation -> "explainLogin".equals(operation.operation())));
            assertEquals("explainLogin", findAuditedOperation("service", serviceUrl).operation());

            val interrupted = casLoginExplanationTools.explainLogin("interrupted-" + UUID.randomUUID(), serviceUrl, null, null, null);
            assertEquals(CasLoginExplanationTools.Outcome.BLOCKED_BY_INTERRUPT, interrupted.outcome(), interrupted::toString);
            assertTrue(interrupted.interrupted());

            val denied = casLoginExplanationTools.explainLogin("casuser", deniedUrl, null, null, null);
            assertEquals(CasLoginExplanationTools.Outcome.ACCESS_DENIED, denied.outcome(), denied::toString);
            assertEquals(1, denied.steps().size());

            val unregistered = casLoginExplanationTools.explainLogin("casuser", "https://unknown.example.net/" + UUID.randomUUID(), null, null, null);
            assertEquals(CasLoginExplanationTools.Outcome.SERVICE_NOT_REGISTERED, unregistered.outcome());

            val risky = casLoginExplanationTools.explainLogin("unknown-" + UUID.randomUUID(), serviceUrl, "198.51.100.7", "curl", null);
            assertEquals(Boolean.TRUE, risky.risky(), risky::toString);
            assertEquals(CasLoginExplanationTools.StepStatus.TRIGGERED, risky.steps().get(1).status());
            assertEquals(TestMultifactorAuthenticationProvider.ID, risky.multifactorProvider(), risky::toString);
        } finally {
            RequestContextHolder.resetRequestAttributes();
            ClientInfoHolder.clear();
        }
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyCompletionsAndServiceResource() {
        val settings = casSettingResources.completeSettingName("server.na");
        assertTrue(settings.values().contains("cas.server.name"), settings::toString);
        assertTrue(casSettingResources.completeSettingName("s").values().isEmpty());

        val registeredService = newRegisteredService(newServiceUrl(), Map.of(), new ReturnAllAttributeReleasePolicy());
        servicesManager.save(registeredService);
        val serviceId = String.valueOf(registeredService.getId());
        assertTrue(casServiceResources.completeServiceId(serviceId).values().contains(serviceId));
        assertTrue(casServiceResources.completeServiceId(registeredService.getName()).values().contains(serviceId));

        val definition = casServiceResources.describeService(serviceId);
        assertEquals(registeredService.getServiceId(), JsonPath.read(definition, "$.serviceId"), definition);
        assertTrue(casServiceResources.describeService("not-a-number").contains("A registered service id is a number"));
        assertTrue(casServiceResources.describeService("-5").contains("No registered service has id"));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindService() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://tools.example.org/" + UUID.randomUUID());
        registeredService.getProperties().put("jwtAsServiceTicketSigningKey", new DefaultRegisteredServiceProperty("secret-signing-key"));
        registeredService.getProperties().put("accessTokenAsJwtSigningKeyEnabled", new DefaultRegisteredServiceProperty("true"));
        servicesManager.save(registeredService);
        val result = casServiceManagementTools.findService(registeredService.getId());
        assertNotNull(result);
        assertFalse(result.contains("secret-signing-key"), result);
        assertTrue(result.contains("<redacted>"), result);
        assertTrue(result.contains("accessTokenAsJwtSigningKeyEnabled"), result);
        assertEquals(registeredService.getId(), JsonPath.<Number>read(result, "$.id").longValue());
        assertEquals(registeredService.getServiceId(), JsonPath.read(result, "$.serviceId"));
        assertNull(casServiceManagementTools.findService(-1));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindTicket() throws Throwable {
        val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketId,
            CoreAuthenticationTestUtils.getAuthentication("casuser", Map.<String, List<Object>>of("memberOf", List.of("secret-group"))),
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticketGrantingTicket);
        val result = casTicketRegistryTools.findTicket(ticketId, null);
        assertEquals("TicketGrantingTicketImpl", result.type(), result::toString);
        assertEquals(TicketGrantingTicket.PREFIX, result.prefix());
        assertEquals(Boolean.FALSE, result.expired());
        assertEquals(Boolean.TRUE, result.root());
        assertEquals(CasMcpSessionReferences.of(ticketId), result.sessionReference());
        val authentication = Objects.requireNonNull(result.authentication());
        assertEquals("casuser", authentication.principal());
        val attributeNames = new HashSet<String>(authentication.principalAttributeNames());
        attributeNames.addAll(authentication.authenticationAttributeNames());
        assertTrue(attributeNames.contains("memberOf"), result::toString);
        assertNoSecrets(result, "secret-group");
        assertNotNull(casTicketRegistryTools.findTicket(UUID.randomUUID().toString(), null).reason());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindPerson() {
        val username = "casuser-" + UUID.randomUUID();
        val person = casPersonDirectoryTools.findPerson(username);
        assertEquals(username, person.id());
        assertNull(person.reason());
        assertEquals(List.of("casuser@apereo.org"), person.attributes().get("mail"));
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
        assertEquals(username, result.username());
        assertEquals(2L, result.count());
        val sessions = result.sessions();
        val references = ticketIds.stream().map(CasMcpSessionReferences::of).collect(Collectors.toSet());
        assertEquals(references, sessions.stream().map(CasTicketRegistryTools.SessionSummary::sessionReference).collect(Collectors.toSet()));
        assertNotNull(sessions.getFirst().authenticationHandlers());
        assertTrue(ticketIds.stream().noneMatch(result.toString()::contains), result::toString);

        val reference = references.iterator().next();
        val byReference = casTicketRegistryTools.findTicket(reference, username);
        assertEquals(reference, byReference.sessionReference(), byReference::toString);
        assertTrue(ticketIds.stream().noneMatch(byReference.toString()::contains), byReference::toString);
        assertEquals("A session reference can only be looked up with the username of the person",
            casTicketRegistryTools.findTicket(reference, null).reason());
        assertNotNull(casTicketRegistryTools.findTicket(reference, "someone-else").reason());
        assertNull(casSingleLogoutTools.previewLogout(reference, username).reason());
        assertEquals(0L, casTicketRegistryTools.findSessions("unknown-" + UUID.randomUUID()).count());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyEvaluateAccess() {
        val allowedUrl = newServiceUrl();
        servicesManager.save(newRegisteredService(allowedUrl,
            Map.of("mail", Set.of("casuser@apereo.org")), new ReturnAllAttributeReleasePolicy()));
        val allowed = casServiceAccessTools.evaluateAccess("casuser", allowedUrl);
        assertTrue(allowed.accessAllowed());
        assertNull(allowed.reason());

        val disabledUrl = newServiceUrl();
        val disabledService = newRegisteredService(disabledUrl, Map.of(), new ReturnAllAttributeReleasePolicy());
        disabledService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        servicesManager.save(disabledService);
        val disabled = casServiceAccessTools.evaluateAccess("casuser", disabledUrl);
        assertFalse(disabled.accessAllowed());
        assertNotNull(disabled.reason());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyPreviewAttributeReleaseForUnknownService() {
        val result = casServiceAccessTools.previewAttributeRelease("casuser", "https://unknown.example.net/" + UUID.randomUUID());
        assertFalse(result.accessAllowed());
        assertNull(result.releasedAttributes());
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
        assertEquals(2, result.count());
        val byId = casServiceManagementTools.searchServices(String.valueOf(first.getId()));
        assertTrue(byId.services().stream().anyMatch(service -> service.id() == first.getId()));
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindSessionsWithAttribute() throws Throwable {
        val group = "group-" + UUID.randomUUID();
        val ticketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TicketGrantingTicket.PREFIX);
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketId,
            CoreAuthenticationTestUtils.getAuthentication("casuser-" + UUID.randomUUID(),
                Map.<String, List<Object>>of("memberOf", List.of(group))), NeverExpiresExpirationPolicy.INSTANCE));
        assertEquals(1, casTicketRegistryTools.findSessionsWithAttribute("memberOf", group).count());
        assertEquals(0, casTicketRegistryTools.findSessionsWithAttribute("memberOf", "missing-" + group).count());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindAuditRecordsForUnknownUser() {
        val result = casAuditTools.findAuditRecords("unknown-" + UUID.randomUUID(), null, null);
        assertEquals(0, result.count());
        assertNotNull(result.since());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindConsentDecisionsForUnknownUser() {
        val result = casConsentTools.findConsentDecisions("unknown-" + UUID.randomUUID());
        assertEquals(0, result.count());
        assertNull(result.reason());
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
            assertEquals(TestMultifactorAuthenticationProvider.ID, Objects.requireNonNull(result.provider()).id());
            assertFalse(result.triggers().isEmpty());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        val providers = casMultifactorAuthenticationTools.listMultifactorProviders().providers();
        assertTrue(providers.stream().anyMatch(provider -> TestMultifactorAuthenticationProvider.ID.equals(provider.id())));
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
            assertEquals(TestMultifactorAuthenticationProvider.ID, Objects.requireNonNull(adaptive.provider()).id(), adaptive::toString);
            val activated = adaptive.triggers().stream()
                .filter(trigger -> "ACTIVATED".equals(trigger.outcome()))
                .map(CasMultifactorAuthenticationTools.TriggerOutcome::trigger)
                .toList();
            assertEquals(1, activated.size(), adaptive::toString);
            assertTrue(activated.getFirst().startsWith("AdaptiveMultifactorAuthenticationTrigger"), adaptive::toString);
            assertEquals("Firefox", adaptive.client().get("userAgent"));
            assertSame(operator, ClientInfoHolder.getClientInfo());

            val unknown = casMultifactorAuthenticationTools.evaluateMultifactorTriggers("casuser", serviceUrl, " ", null, null);
            assertNull(unknown.provider(), unknown::toString);
            assertNull(unknown.client().get("clientIpAddress"));
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
        assertEquals(1, result.count(), result::toString);
        val account = result.accounts().getFirst();
        assertEquals("Work phone", account.name());
        assertEquals(2, account.scratchCodesLeft());
        assertEquals("JSON", account.source());
        assertNoSecrets(result, "secret-otp-key", "918273", "55667788", "99887766");
        assertEquals(0, casOneTimeTokenTools.findOneTimeTokenAccounts("unknown-" + UUID.randomUUID()).count());
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
        assertEquals(1, result.count(), result::toString);
        val device = result.devices().getFirst();
        assertEquals("YubiKey 5", device.nickname());
        assertEquals(List.of("usb"), device.transports());
        assertEquals(Boolean.TRUE, device.discoverable());
        assertEquals(7L, device.signatureCount());
        assertNoSecrets(result, credentialId, userHandle, publicKey);
        assertEquals(0, casWebAuthnTools.findWebAuthnDevices("unknown-" + UUID.randomUUID()).count());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindPasswordlessAccount() {
        val username = "passwordless-" + UUID.randomUUID();
        val result = casPasswordlessTools.findPasswordlessAccount(username);
        val account = Objects.requireNonNull(result.account(), result::toString);
        assertEquals("j*****@example.org", account.email());
        assertEquals("********23", account.phone());
        assertTrue(account.requestPassword());
        assertEquals(List.of("Okta"), account.allowedDelegatedClients());
        assertNoSecrets(result, "jsmith@example.org", "3477464523", "attribute-secret");
        assertNotNull(casPasswordlessTools.findPasswordlessAccount("casuser").reason());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyListIdentityProviders() {
        assertThrows(IllegalStateException.class, () -> casDelegatedAuthenticationTools.listIdentityProviders());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        try {
            val result = casDelegatedAuthenticationTools.listIdentityProviders();
            assertEquals(2, result.count(), result::toString);
            val providers = result.identityProviders();
            assertEquals(List.of("AnonymousClient", "Okta"), providers.stream().map(CasDelegatedAuthenticationTools.IdentityProvider::name).toList());
            val okta = providers.getLast();
            assertEquals("Okta Workforce", okta.displayName());
            assertEquals("CLIENT", okta.autoRedirectType());
            assertEquals("https://sso.example.org/cas/login?client_name=Okta", okta.callbackUrl());
            assertTrue(okta.type().startsWith("IndirectClient"), okta::toString);
            assertNoSecrets(result, "delegated-client-secret");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindAuthorizableResources() {
        val result = casHeimdallTools.findAuthorizableResources("API");
        assertEquals(1, result.count(), result::toString);
        val resource = result.resources().getFirst();
        assertEquals("/api/.*", resource.pattern());
        assertEquals("GET", resource.method());
        assertEquals(List.of("read"), resource.actions());
        assertEquals(List.of("JdbcAuthorizationPolicy"), resource.policies());
        assertEquals(List.of("owner"), resource.propertyNames());
        assertNoSecrets(result, "jdbc-secret", "jdbc:hsqldb:mem:heimdall", "owner-secret");

        val unknown = casHeimdallTools.findAuthorizableResources("unknown");
        assertNotNull(unknown.reason());
        assertEquals(List.of("API"), unknown.knownNamespaces());
        assertNull(casHeimdallTools.findAuthorizableResources(null).reason());
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

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifySearchSettings() {
        val all = casSettingTools.searchSettings("Server NAME", null);
        assertEquals(2, all.count(), all::toString);
        assertFalse(all.truncated());
        val names = all.settings().stream().map(CasSettingTools.SettingSummary::name).toList();
        assertEquals(List.of("cas.server.name", "spring.ai.mcp.server.name"), names);

        val core = casSettingTools.searchSettings("server name", "cas-server-core");
        assertEquals(1, core.count(), core::toString);
        val setting = core.settings().getFirst();
        assertEquals("cas://settings/cas.server.name", setting.uri());
        assertEquals("cas-server-core", setting.module());
        assertEquals("Full name of the CAS server.", setting.description());

        assertNotNull(casSettingTools.searchSettings("ab", null).reason());
        assertEquals(0, casSettingTools.searchSettings("no-such-setting", null).count());
    }

    @Test
    @WithMockUser(username = "operator", roles = "MCP_ADMIN")
    void verifyCallsAreAudited() {
        val username = "audited-" + UUID.randomUUID();
        casPersonDirectoryTools.findPerson(username);
        val success = findAuditedOperation("username", username);
        assertEquals("findPerson", success.operation());
        assertEquals("operator", success.operator());
        assertEquals(CasMcpOperation.Outcome.SUCCESS, success.outcome());

        val serviceUrl = newServiceUrl();
        assertThrows(IllegalStateException.class,
            () -> casMultifactorAuthenticationTools.evaluateMultifactorTriggers(username, serviceUrl, null, null, null));
        val failure = findAuditedOperation("service", serviceUrl);
        assertEquals("evaluateMultifactorTriggers", failure.operation());
        assertEquals(CasMcpOperation.Outcome.FAILED, failure.outcome());

        val settingName = "cas.audited." + UUID.randomUUID();
        casSettingResources.describeSetting(settingName);
        assertEquals(CasSettingResources.SETTING_URI_TEMPLATE, findAuditedOperation("name", settingName).operation());
    }

    @Test
    @WithMockUser(username = "intruder", roles = "USER")
    void verifyDeniedCallsAreAudited() {
        val username = "denied-" + UUID.randomUUID();
        assertThrows(AccessDeniedException.class, () -> casPersonDirectoryTools.findPerson(username));
        val denied = findAuditedOperation("username", username);
        assertEquals("intruder", denied.operator());
        assertEquals(CasMcpOperation.Outcome.DENIED, denied.outcome());
    }

    @Test
    void verifyAuditResolvers() {
        val operation = new CasMcpOperation("operator", "findPerson", Map.of("username", "casuser"), CasMcpOperation.Outcome.DENIED);
        assertEquals("operator", operation.getAuditablePrincipal());
        val audit = mock(Audit.class);
        when(audit.action()).thenReturn(CasMcpAuditor.AUDIT_ACTION);
        val joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{operation});
        val actionResolver = new CasMcpOperationAuditActionResolver();
        assertEquals("MCP_TOOL_CALL_DENIED", actionResolver.resolveFrom(joinPoint, operation, audit));
        assertEquals("MCP_TOOL_CALL_FAILED", actionResolver.resolveFrom(joinPoint, new IllegalStateException(), audit));
        val resourceResolver = new CasMcpOperationAuditResourceResolver(MessageSanitizer.disabled());
        assertEquals("operation=findPerson, arguments={username=casuser}", resourceResolver.resolveFrom(joinPoint, operation)[0]);
        assertEquals("operation=findPerson, arguments={username=casuser}", resourceResolver.resolveFrom(joinPoint, new IllegalStateException())[0]);
        val sanitizing = new CasMcpOperationAuditResourceResolver(message -> message.replace("casuser", "*******"));
        assertEquals("operation=findPerson, arguments={username=*******}", sanitizing.resolveFrom(joinPoint, operation)[0]);
    }

    private CasMcpOperation findAuditedOperation(final String argument, final String value) {
        return casMcpAuditor.getOperations()
            .stream()
            .filter(operation -> value.equals(operation.arguments().get(argument)))
            .reduce((first, second) -> second)
            .orElseThrow(() -> new AssertionError("No audited MCP call has " + argument + '=' + value));
    }

    private static void assertNoSecrets(final Object result, final String... secrets) {
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
        val logout = casSingleLogoutTools.previewLogout(ticketId, null);
        assertEquals(0, logout.count());
        assertFalse(logout.singleLogoutDisabled());
        assertNotNull(casSingleLogoutTools.previewLogout("TGT-" + UUID.randomUUID(), null).reason());

        val statistics = casServerStatisticsTools.getRegistryStatistics();
        assertTrue(Objects.requireNonNull(statistics.singleSignOnSessions()) >= 1);
        assertNotNull(statistics.ticketRegistry());
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
        assertEquals(1, result.count(), result::toString);
        val event = result.events().getFirst();
        assertEquals("CasTicketGrantingTicketCreatedEvent", event.type());
        assertEquals("192.0.2.10", event.clientIpAddress());
        assertEquals("Firefox", event.userAgent());
        assertFalse(result.toString().contains("secret-device-fingerprint"));
        assertEquals(2, casAuthenticationEventTools.findAuthenticationEvents(username, 30).count());
    }

    @Test
    void verifyFindAuthenticationEventsWhenEventsAreOff() {
        val repository = BeanSupplier.of(CasEventRepository.class).neverMatch().otherwiseProxy().get();
        val beanFactory = new StaticListableBeanFactory(Map.<String, Object>of(CasEventRepository.BEAN_NAME, repository));
        val tools = new CasAuthenticationEventTools(beanFactory.getBeanProvider(CasEventRepository.class));
        assertEquals("Authentication events are not recorded", tools.findAuthenticationEvents("casuser", null).reason());
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
        assertEquals(1, result.count(), result::toString);
        val device = result.devices().getFirst();
        assertEquals(trustRecord.getName(), device.name());
        assertEquals(TestMultifactorAuthenticationProvider.ID, device.multifactorAuthenticationProvider());
        assertFalse(device.expired());
        assertNotNull(device.expirationDate());
        assertFalse(result.toString().contains("secret-device-fingerprint"));
        assertFalse(result.toString().contains("secret-record-key"));
        assertEquals(0, casTrustedDeviceTools.findTrustedDevices("unknown-" + UUID.randomUUID()).count());
    }

    @Test
    @WithMockUser(roles = "MCP_ADMIN")
    void verifyFindSurrogateAccounts() {
        val result = casSurrogateTools.findSurrogateAccounts("casuser", null);
        assertFalse(result.anyAccount());
        assertEquals(List.of("banderson", "jsmith"), result.accounts());
        assertNull(result.service());

        val serviceUrl = newServiceUrl();
        val forService = casSurrogateTools.findSurrogateAccounts("casuser", serviceUrl);
        assertEquals(serviceUrl, forService.service());
        assertEquals(1, forService.count());

        val wildcard = casSurrogateTools.findSurrogateAccounts("surrogate-admin", StringUtils.EMPTY);
        assertTrue(wildcard.anyAccount());
        assertEquals(List.of("jsmith"), wildcard.accounts());
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
            val outcomes = interrupted.inquirers();
            assertEquals(List.of("FailingInquirer", "TermsInquirer"), outcomes.stream().map(CasInterruptTools.InquirerOutcome::inquirer).toList());
            assertEquals(List.of("FAILED", "INTERRUPT"), outcomes.stream().map(CasInterruptTools.InquirerOutcome::outcome).toList());
            assertEquals("Interrupt endpoint is unavailable", outcomes.getFirst().error());
            val interrupt = Objects.requireNonNull(interrupted.interrupt());
            assertEquals("Accept the terms of " + serviceUrl + " as " + username, interrupt.message());
            assertTrue(interrupt.block());
            assertEquals(Map.of("Terms", "https://terms.example.org"), interrupt.links());
            assertTrue(Objects.requireNonNull(interrupted.interruptPolicy()).enabled());

            val quiet = casInterruptTools.previewInterrupt("casuser", serviceUrl);
            assertNull(quiet.interrupt());
            assertEquals(List.of("FAILED", "NO_INTERRUPT", "NO_INTERRUPT"),
                quiet.inquirers().stream().map(CasInterruptTools.InquirerOutcome::outcome).toList());
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
        assertEquals(0, BigDecimal.ZERO.compareTo(Objects.requireNonNull(familiar.score())), familiar::toString);
        assertEquals(Boolean.FALSE, familiar.risky());
        assertEquals(Double.valueOf(0.6), familiar.threshold());
        assertEquals(List.of("IpAddressAuthenticationRequestRiskCalculator", "UserAgentAuthenticationRequestRiskCalculator"),
            familiar.calculators().stream().map(CasAuthenticationRiskTools.CalculatorScore::calculator).toList());

        val unfamiliar = casAuthenticationRiskTools.evaluateAuthenticationRisk(username, serviceUrl, "198.51.100.7", "curl", "48.85,2.35");
        assertEquals(0, BigDecimal.ONE.compareTo(Objects.requireNonNull(unfamiliar.score())), unfamiliar::toString);
        assertEquals(Boolean.TRUE, unfamiliar.risky());
        assertEquals("48.85,2.35", unfamiliar.client().get("geoLocation"));

        val unknown = casAuthenticationRiskTools.evaluateAuthenticationRisk("unknown-" + UUID.randomUUID(), serviceUrl, "192.0.2.10", "Firefox", null);
        assertEquals(Boolean.TRUE, unknown.risky());
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
        assertNotNull(result.reason());
        assertNotNull(result.knownTenants());
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyToolsDenyUsersWithoutRole() {
        assertThrows(AccessDeniedException.class, () -> casServiceManagementTools.findService(1));
        assertThrows(AccessDeniedException.class, () -> casTicketRegistryTools.findTicket("TGT-1", null));
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
        assertThrows(AccessDeniedException.class, () -> casSettingTools.searchSettings("server", null));
        assertThrows(AccessDeniedException.class, () -> casSingleLogoutTools.previewLogout("TGT-1", null));
        assertThrows(AccessDeniedException.class, () -> casServerStatisticsTools.getRegistryStatistics());
        assertThrows(AccessDeniedException.class, () -> casTenantTools.findTenant("unknown"));
        assertThrows(AccessDeniedException.class, () -> casAuthenticationEventTools.findAuthenticationEvents("casuser", null));
        assertThrows(AccessDeniedException.class, () -> casTrustedDeviceTools.findTrustedDevices("casuser"));
        assertThrows(AccessDeniedException.class, () -> casSurrogateTools.findSurrogateAccounts("casuser", null));
        assertThrows(AccessDeniedException.class, () -> casInterruptTools.previewInterrupt("casuser", "https://mcp.example.org"));
        assertThrows(AccessDeniedException.class,
            () -> casAuthenticationRiskTools.evaluateAuthenticationRisk("casuser", "https://mcp.example.org", "192.0.2.10", "Firefox", null));
        assertThrows(AccessDeniedException.class, () -> casDeploymentTools.describeDeployment());
        assertThrows(AccessDeniedException.class, () -> casThrottleTools.findThrottledLogins("casuser", null));
        assertThrows(AccessDeniedException.class, () -> casAttributeTools.findAttributeSources("casuser"));
        assertThrows(AccessDeniedException.class, () -> casAttributeTools.findAttributeDefinitions(null));
        assertThrows(AccessDeniedException.class, () -> casServiceManagementTools.validateServiceDefinition("{}", null));
        assertThrows(AccessDeniedException.class,
            () -> casLoginExplanationTools.explainLogin("casuser", "https://mcp.example.org", null, null, null));
        assertThrows(AccessDeniedException.class, () -> casServiceResources.describeService("1"));
        assertThrows(AccessDeniedException.class, () -> casServiceResources.completeServiceId("1"));
        assertThrows(AccessDeniedException.class, () -> casSettingResources.completeSettingName("cas.server"));
    }

    @Test
    void verifyToolsRequireAuthentication() {
        assertThrows(AuthenticationException.class, () -> casServiceManagementTools.findService(1));
        assertThrows(AuthenticationException.class, () -> casTicketRegistryTools.findTicket("TGT-1", null));
        assertThrows(AuthenticationException.class, () -> casPersonDirectoryTools.findPerson("casuser"));
        assertThrows(AuthenticationException.class, () -> casServiceAccessTools.evaluateAccess("casuser", "https://mcp.example.org"));
    }
}
