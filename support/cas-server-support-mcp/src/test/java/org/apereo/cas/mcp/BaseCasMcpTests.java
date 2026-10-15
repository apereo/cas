package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailExecutionPlan;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasMcpAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.DefaultConsentDecisionBuilder;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculator;
import org.apereo.cas.impl.engine.DefaultAuthenticationRiskEvaluator;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.jayway.jsonpath.JsonPath;
import com.yubico.data.CredentialRegistration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.client.direct.AnonymousClient;
import org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.McpServerJsonMapperAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerSpecificationFactoryAutoConfiguration;
import org.springframework.ai.mcp.server.webmvc.autoconfigure.McpServerStreamableHttpWebMvcAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.execution.RequestContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link BaseCasMcpTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = {
    "spring.ai.mcp.server.name=Apereo CAS MCP Server",
    "spring.ai.mcp.server.protocol=STREAMABLE",
    "spring.ai.mcp.server.capabilities.resource=true",
    "spring.ai.mcp.server.capabilities.prompt=false",
    "spring.ai.mcp.server.capabilities.completion=false",
    "cas.monitor.endpoints.json.location=classpath:/McpUserDetails.json",
    "cas.authn.attribute-repository.stub.attributes.mail=casuser@apereo.org",
    "cas.authn.adaptive.policy.require-multifactor.mfa-dummy=198[.]51[.]100[.][0-9]+"
})
public abstract class BaseCasMcpTests {
    protected static final String MCP_ENDPOINT = "/mcp";

    protected static final String MCP_SESSION_ID = "Mcp-Session-Id";

    protected static final RequestPostProcessor MCP_ADMIN = httpBasic("mcpadmin", "Mellon");

    protected static final RequestPostProcessor CAS_USER = httpBasic("casuser", "Mellon");

    private static final AtomicLong REQUEST_ID = new AtomicLong();

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    protected static String newServiceUrl() {
        return "https://mcp.example.org/" + UUID.randomUUID();
    }

    protected static CasRegisteredService newRegisteredService(final String serviceUrl,
                                                               final Map<String, Set<String>> requiredAttributes,
                                                               final RegisteredServiceAttributeReleasePolicy attributeReleasePolicy) {
        val registeredService = new CasRegisteredService();
        registeredService.setId(ThreadLocalRandom.current().nextLong(1, Integer.MAX_VALUE));
        registeredService.setName("MCP-" + registeredService.getId());
        registeredService.setServiceId(serviceUrl + ".*");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(requiredAttributes));
        registeredService.setAttributeReleasePolicy(attributeReleasePolicy);
        return registeredService;
    }

    protected static String jsonRpcRequest(final String method, final String params) {
        return """
            {"jsonrpc":"2.0","id":%s,"method":"%s","params":%s}
            """.formatted(REQUEST_ID.incrementAndGet(), method, params);
    }

    protected static String callToolRequest(final String tool, final String arguments) {
        return jsonRpcRequest("tools/call", """
            {"name":"%s","arguments":%s}
            """.formatted(tool, arguments));
    }

    protected String initialize(final RequestPostProcessor user) throws Exception {
        val initialize = jsonRpcRequest("initialize", """
            {"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"cas-tests","version":"1.0.0"}}
            """);
        val result = mockMvc.perform(post(MCP_ENDPOINT)
                .with(user)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .content(initialize))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.serverInfo.name").value("Apereo CAS MCP Server"))
            .andExpect(jsonPath("$.result.capabilities.tools").exists())
            .andReturn();
        val sessionId = result.getResponse().getHeader(MCP_SESSION_ID);
        assertNotNull(sessionId);
        mockMvc.perform(post(MCP_ENDPOINT)
                .with(user)
                .header(MCP_SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .content("""
                    {"jsonrpc":"2.0","method":"notifications/initialized"}
                    """))
            .andExpect(status().isAccepted());
        return sessionId;
    }

    protected String send(final RequestPostProcessor user, final String sessionId, final String request) throws Exception {
        val result = mockMvc.perform(post(MCP_ENDPOINT)
                .with(user)
                .header(MCP_SESSION_ID, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .content(request))
            .andExpect(status().isOk())
            .andReturn();
        val content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        val message = content.lines()
            .filter(line -> line.startsWith("data:"))
            .map(line -> line.substring("data:".length()).trim())
            .reduce((first, second) -> second)
            .orElseThrow(() -> new AssertionError("No server-sent event found in response " + content));
        assertTrue(JsonPath.<Map<String, Object>>read(message, "$").containsKey("result"), message);
        return message;
    }

    protected static boolean isToolError(final String response) {
        val result = JsonPath.<Map<String, Object>>read(response, "$.result");
        return Boolean.TRUE.equals(result.get("isError"));
    }

    protected List<String> listTools(final RequestPostProcessor user) throws Exception {
        val response = send(user, initialize(user), jsonRpcRequest("tools/list", "{}"));
        return JsonPath.read(response, "$.result.tools[*].name");
    }
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasWebAppAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasMcpAutoConfiguration.class,
        McpServerJsonMapperAutoConfiguration.class,
        McpServerAnnotationScannerAutoConfiguration.class,
        McpServerSpecificationFactoryAutoConfiguration.class,
        McpServerStreamableHttpWebMvcAutoConfiguration.class,
        McpServerAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import({McpToolsTestConfiguration.class, McpFederationTestConfiguration.class})
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "McpToolsTestConfiguration", proxyBeanMethods = false)
    public static class McpToolsTestConfiguration {
        @Bean
        public AuditTrailManager mcpMockAuditTrailManager() {
            return new MockAuditTrailManager();
        }

        @Bean(name = AuditTrailExecutionPlan.BEAN_NAME)
        public AuditTrailExecutionPlan auditTrailExecutionPlan(
            @Qualifier("mcpMockAuditTrailManager")
            final AuditTrailManager mcpMockAuditTrailManager) {
            val plan = new DefaultAuditTrailExecutionPlan();
            plan.registerAuditTrailManager(mcpMockAuditTrailManager);
            plan.registerAuditTrailManager(new AuditTrailManager() {
                @Override
                public void record(final AuditActionContext auditActionContext) {
                }

                @Override
                public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
                    val count = (long) whereClause.get(WhereClauseFields.COUNT);
                    assertTrue(count > 0);
                    return List.of();
                }

                @Override
                public void removeAll() {
                }
            });
            return plan;
        }

        @Bean
        public MultifactorAuthenticationProvider mcpTestMultifactorAuthenticationProvider() {
            return new TestMultifactorAuthenticationProvider();
        }

        @Bean(name = ConsentRepository.BEAN_NAME)
        public ConsentRepository consentRepository() {
            return new InMemoryConsentRepository();
        }

        @Bean(name = ConsentDecisionBuilder.BEAN_NAME)
        public ConsentDecisionBuilder consentDecisionBuilder() {
            return new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        }

        @Bean(name = CasEventRepository.BEAN_NAME)
        public CasEventRepository casEventRepository() {
            val events = new CopyOnWriteArrayList<CasEvent>();
            return new CasEventRepository() {
                @Override
                public CasEvent save(final CasEvent event) {
                    events.add(event);
                    return event;
                }

                @Override
                public Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
                    return events.stream().filter(event -> event.getPrincipalId().equals(id));
                }

                @Override
                public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                              final ZonedDateTime dateTime) {
                    return events.stream().filter(event -> event.getPrincipalId().equals(principal) && event.getType().equals(type));
                }
            };
        }

        @Bean(name = MultifactorAuthenticationTrustStorage.BEAN_NAME)
        public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
            val records = new CopyOnWriteArrayList<MultifactorAuthenticationTrustRecord>();
            val storage = mock(MultifactorAuthenticationTrustStorage.class);
            when(storage.save(any())).thenAnswer(invocation -> {
                val trustRecord = invocation.getArgument(0, MultifactorAuthenticationTrustRecord.class);
                records.add(trustRecord);
                return trustRecord;
            });
            when(storage.get(anyString())).thenAnswer(invocation -> records.stream()
                .filter(trustRecord -> trustRecord.getPrincipal().equals(invocation.getArgument(0)))
                .collect(Collectors.toSet()));
            return storage;
        }

        @Bean(name = InterruptInquirer.BEAN_NAME)
        public InterruptInquiryExecutionPlan interruptInquirer() {
            val inquirers = new CopyOnWriteArrayList<InterruptInquirer>();
            val plan = new InterruptInquiryExecutionPlan() {
                @Override
                public void registerInterruptInquirer(final InterruptInquirer inquirer) {
                    inquirers.add(inquirer);
                }

                @Override
                public List<InterruptInquirer> getInterruptInquirers() {
                    return List.copyOf(inquirers);
                }
            };
            plan.registerInterruptInquirer(new McpTestInterruptInquirer("FailingInquirer", (authentication, requestContext) -> {
                throw new IllegalStateException("Interrupt endpoint is unavailable");
            }));
            plan.registerInterruptInquirer(new McpTestInterruptInquirer("TermsInquirer", (authentication, requestContext) -> {
                if (!authentication.getPrincipal().getId().startsWith("interrupted-")) {
                    return InterruptResponse.none();
                }
                val service = Objects.requireNonNull(WebUtils.getService(requestContext));
                val credential = Objects.requireNonNull(WebUtils.getCredential(requestContext));
                return new InterruptResponse("Accept the terms of " + service.getId() + " as " + credential.getId())
                    .setLinks(Map.of("Terms", "https://terms.example.org"))
                    .setBlock(true)
                    .setInterrupt(true);
            }));
            plan.registerInterruptInquirer(new McpTestInterruptInquirer("QuietInquirer", (authentication, requestContext) -> InterruptResponse.none()));
            return plan;
        }

        @Bean(name = CasEventRepository.TRANSACTION_MANAGER_EVENTS)
        public PlatformTransactionManager transactionManagerEvents() {
            return new PseudoTransactionManager();
        }

        @Bean(name = "authenticationRiskEvaluator")
        public AuthenticationRiskEvaluator authenticationRiskEvaluator(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasEventRepository.BEAN_NAME)
            final CasEventRepository casEventRepository) {
            return new DefaultAuthenticationRiskEvaluator(List.of(
                new IpAddressAuthenticationRequestRiskCalculator(casEventRepository, casProperties),
                new UserAgentAuthenticationRequestRiskCalculator(casEventRepository, casProperties)), casProperties, casEventRepository);
        }

        @Bean(name = "googleAuthenticatorAccountRegistry")
        public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry() {
            val accounts = new CopyOnWriteArrayList<OneTimeTokenAccount>();
            val repository = mock(OneTimeTokenCredentialRepository.class);
            when(repository.save(any())).thenAnswer(invocation -> {
                val account = invocation.getArgument(0, OneTimeTokenAccount.class);
                accounts.add(account);
                return account;
            });
            when(repository.get(anyString())).thenAnswer(invocation -> accounts.stream()
                .filter(account -> account.getUsername().equals(invocation.getArgument(0)))
                .toList());
            return repository;
        }

        @Bean(name = WebAuthnCredentialRepository.BEAN_NAME)
        public WebAuthnCredentialRepository webAuthnCredentialRepository() {
            val registrations = new CopyOnWriteArrayList<CredentialRegistration>();
            val repository = mock(WebAuthnCredentialRepository.class);
            when(repository.addRegistrationByUsername(anyString(), any())).thenAnswer(invocation ->
                registrations.add(invocation.getArgument(1, CredentialRegistration.class)));
            when(repository.getRegistrationsByUsername(anyString())).thenAnswer(invocation -> registrations.stream()
                .filter(registration -> registration.getUsername().equals(invocation.getArgument(0)))
                .toList());
            return repository;
        }

        @Bean(name = PasswordlessUserAccountStore.BEAN_NAME)
        public PasswordlessUserAccountStore passwordlessUserAccountStore() {
            return request -> request.getUsername().startsWith("passwordless-")
                ? Optional.of(PasswordlessUserAccount.builder()
                    .username(request.getUsername())
                    .name("Passwordless User")
                    .email("jsmith@example.org")
                    .phone("3477464523")
                    .requestPassword(true)
                    .allowedDelegatedClients(List.of("Okta"))
                    .attributes(Map.of("secretAttribute", List.<Object>of("attribute-secret")))
                    .source("JSON")
                    .build())
                : Optional.empty();
        }

        @Bean(name = DelegatedIdentityProviders.BEAN_NAME)
        public DelegatedIdentityProviders delegatedIdentityProviders() {
            val okta = mock(IndirectClient.class);
            when(okta.getName()).thenReturn("Okta");
            when(okta.getCallbackUrl()).thenReturn("https://sso.example.org/cas/login?client_name=Okta");
            when(okta.getCustomProperties()).thenReturn(Map.of(
                ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, "Okta Workforce",
                ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, "CLIENT",
                "clientSecret", "delegated-client-secret"));
            return (service, webContext) -> List.of(okta, new AnonymousClient());
        }

        @Bean(name = AuthorizableResourceRepository.BEAN_NAME)
        public AuthorizableResourceRepository authorizableResourceRepository() {
            val resource = new AuthorizableResource()
                .setId(1)
                .setPattern(Pattern.compile("/api/.*"))
                .setMethod("GET")
                .setActions(Set.of("read"))
                .setEnforceAllPolicies(true)
                .setPolicies(List.of(new JdbcAuthorizationPolicy().setUrl("jdbc:hsqldb:mem:heimdall")
                    .setUsername("sa").setPassword("jdbc-secret").setQuery("select 1")))
                .setProperties(Map.of("owner", "owner-secret"));
            val repository = mock(AuthorizableResourceRepository.class);
            when(repository.find("API")).thenReturn(List.of(resource));
            when(repository.findAll()).thenReturn(Map.of("API", List.of(resource)));
            return repository;
        }

        @Bean(name = CasConfigurationMetadataRepository.BEAN_NAME)
        public CasConfigurationMetadataRepository casConfigurationMetadataRepository() throws Exception {
            try (val in = new ClassPathResource("McpSettingsMetadata.json").getInputStream()) {
                return new CasConfigurationMetadataRepository(List.of(in.readAllBytes()));
            }
        }

        @Bean(name = SurrogateAuthenticationService.BEAN_NAME)
        public SurrogateAuthenticationService surrogateAuthenticationService() {
            return (username, service) -> {
                if (username.startsWith("surrogate-admin")) {
                    return List.of(SurrogateAuthenticationService.WILDCARD_ACCOUNT, "jsmith");
                }
                return service.isPresent() ? List.of("jsmith") : List.of("jsmith", "banderson");
            };
        }
    }

    @RequiredArgsConstructor
    private static final class McpTestInterruptInquirer implements InterruptInquirer {
        @Getter
        private final String name;

        private final BiFunction<Authentication, RequestContext, InterruptResponse> inquiry;

        @Override
        public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService,
                                         final Service service, final Credential credential, final RequestContext requestContext) {
            return inquiry.apply(authentication, requestContext);
        }
    }
}
