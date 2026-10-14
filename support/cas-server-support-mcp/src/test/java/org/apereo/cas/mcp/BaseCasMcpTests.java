package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.audit.spi.plan.DefaultAuditTrailExecutionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
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
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
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
    "spring.ai.mcp.server.capabilities.resource=false",
    "spring.ai.mcp.server.capabilities.prompt=false",
    "spring.ai.mcp.server.capabilities.completion=false",
    "cas.monitor.endpoints.json.location=classpath:/McpUserDetails.json",
    "cas.authn.attribute-repository.stub.attributes.mail=casuser@apereo.org"
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
    }
}
