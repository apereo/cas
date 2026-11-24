package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.palantir.overview.PalantirApplicationChanges;
import org.apereo.cas.palantir.overview.PalantirOverview;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.InetAddressUtils;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DashboardControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */

@SpringBootTest(classes = BasePalantirTests.SharedTestConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class DashboardControllerTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mvc;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> serviceTemplates() throws Exception {
        val authentication = new UsernamePasswordAuthenticationToken("casuser", "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        val model = mvc.perform(get("/palantir/dashboard").with(authentication(authentication)))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView()
            .getModel();
        return (List<Map<String, String>>) model.get("serviceTemplates");
    }

    private static void assertFieldsKept(final String path, final JsonNode template, final JsonNode read) {
        template.properties().forEach(entry -> {
            val name = "clientSecret".equals(entry.getKey()) ? "clientSecrets" : entry.getKey();
            assertTrue(read.has(name), () -> path + " dropped " + entry.getKey() + " in " + read);
            if (entry.getValue().isObject() && read.get(name).isObject()) {
                assertFieldsKept(path + '.' + name, entry.getValue(), read.get(name));
            }
        });
    }

    @Test
    void verifyBuiltInTemplatesAreReadWithoutLosingFields() throws Throwable {
        val files = new PathMatchingResourcePatternResolver().getResources("classpath:service-definitions/**/*.json");
        val templates = serviceTemplates().stream().filter(template -> "builtin".equals(template.get("source"))).toList();
        assertEquals(files.length, templates.size());
        assertTrue(templates.stream().map(template -> template.get("type")).distinct().count() >= 5);

        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val mapper = JsonMapper.builder().build();
        for (val template : templates) {
            val service = serializer.from(template.get("definition"));
            assertNotNull(service, () -> template.get("definition"));
            assertNotNull(service.getName());
            assertNotNull(service.getDescription());
            assertFieldsKept(service.getName(), mapper.readTree(template.get("definition")), mapper.readTree(serializer.toString(service)));
        }
    }
    
    @Test
    void verifyOperation() throws Throwable {
        val authentication = new UsernamePasswordAuthenticationToken(
            "casuser",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        val model = mvc.perform(get("/palantir/dashboard")
                .with(authentication(authentication)))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView()
            .getModel();
        assertNotNull(model.get("casServerPrefix"));
        assertTrue(model.containsKey("authentication"));
        assertFalse(model.containsKey("httpRequestMethod"));
        assertFalse(model.containsKey("httpRequestSecure"));
        assertFalse(model.containsKey("httpRequestHeaders"));
        assertEquals(Boolean.TRUE, model.get("isRoleAdmin"));
        assertTrue(model.containsKey("actuatorEndpoints"));
        assertTrue(model.containsKey("actuatorEndpointAccess"));
        assertTrue(model.containsKey("serviceTemplates"));
        val serviceProperties = ((List<?>) model.get("serviceProperties")).stream().map(entry -> (Map<?, ?>) entry).toList();
        assertTrue(serviceProperties.stream().anyMatch(property ->
            RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName().equals(property.get("name"))
                && !String.valueOf(property.get("description")).isBlank()));
        assertTrue(serviceProperties.stream().noneMatch(property -> "TOKEN_AS_SERVICE_TICKET".equals(property.get("name"))));
        assertEquals(Boolean.FALSE, model.get("configurationEncryptionAvailable"));
        val strategies = (Collection<?>) model.get("supportedAccessStrategies");
        assertTrue(strategies.contains(DefaultRegisteredServiceAccessStrategy.class.getName()));
        assertFalse(strategies.contains("org.apereo.cas.permit.PermitRegisteredServiceAccessStrategy"));

        mvc.perform(get("/palantir/").with(authentication(authentication))).andExpect(status().isOk());
        mvc.perform(get("/palantir").with(authentication(authentication))).andExpect(status().isOk());

    }

    @Test
    void verifySession() throws Throwable {
        val authentication = new UsernamePasswordAuthenticationToken(
            "casuser",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        val session = new MockHttpSession();
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.createEmptyContext()
        );

        mvc.perform(get("/palantir/dashboard/session")
            .with(authentication(authentication))
            .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("casuser"))
            .andExpect(jsonPath("$.id").doesNotExist());
        SecurityContextHolder.clearContext();
        mvc.perform(get("/palantir/dashboard/session"))
            .andExpect(status().is3xxRedirection());
    }

    /**
     * The home page summary answers the server for everyone signed in and keeps the sections about people and
     * applications to administrators.
     */
    @Test
    void verifyOverview() throws Throwable {
        val administrator = new UsernamePasswordAuthenticationToken("casadmin", "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        mvc.perform(get("/palantir/dashboard/overview").with(authentication(administrator)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generated").exists())
            .andExpect(jsonPath("$.sections.server.version").exists())
            .andExpect(jsonPath("$.sections.server.uptimeSeconds").isNumber())
            .andExpect(jsonPath("$.sections.server.host").value(InetAddressUtils.getCasServerHostName()));

        val user = new UsernamePasswordAuthenticationToken("casuser", "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER")));
        mvc.perform(get("/palantir/dashboard/overview").with(authentication(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sections.server").exists())
            .andExpect(jsonPath("$.sections.applications").doesNotExist())
            .andExpect(jsonPath("$.sections.tickets").doesNotExist());

        mvc.perform(get("/palantir/dashboard/overview")).andExpect(status().is3xxRedirection());
    }

    /**
     * The home page can ask for some sections only, and the slowest URLs leave out Palantir's own, actuator and static
     * requests, slowest by average first.
     */
    @Test
    @SuppressWarnings("unchecked")
    void verifyOverviewSlowestEndpointsAndChosenSections() {
        val registry = new SimpleMeterRegistry();
        Map.of("/login", 400L, "/oidc/token", 50L, "/actuator/health", 900L, "/palantir/dashboard/overview", 2000L, "/js/cas.js", 3000L, "/**", 2500L)
            .forEach((uri, millis) -> Timer.builder("http.server.requests").tag("uri", uri).tag("outcome", "SUCCESS")
                .register(registry).record(Duration.ofMillis(millis)));
        Timer.builder("http.server.requests").tag("uri", "/login").tag("outcome", "SERVER_ERROR")
            .register(registry).record(Duration.ofMillis(800));
        val overview = new PalantirOverview(applicationContext,
            applicationContext.getBeanProvider(ServicesManager.class), applicationContext.getBeanProvider(TicketRegistry.class),
            new StaticListableBeanFactory(Map.of("meterRegistry", registry)).getBeanProvider(MeterRegistry.class),
            applicationContext.getBeanProvider(PalantirApplicationChanges.class));
        try {
            val sections = (Map<String, Object>) overview.summarize(Set.of("metrics"), true, Set.of("endpoints")).get("sections");
            assertEquals(Set.of("endpoints"), sections.keySet());
            val slowest = (List<PalantirOverview.SlowEndpoint>) ((Map<String, Object>) sections.get("endpoints")).get("slowest");
            assertEquals(List.of("/login", "/oidc/token"), slowest.stream().map(PalantirOverview.SlowEndpoint::uri).toList());
            assertEquals(2, slowest.getFirst().count());
            assertEquals(.6, slowest.getFirst().meanSeconds(), .01);
            assertEquals(1, slowest.getFirst().errors());
            val user = (Map<String, Object>) overview.summarize(Set.of("metrics"), false, Set.of("endpoints")).get("sections");
            assertTrue(user.isEmpty());
        } finally {
            overview.destroy();
        }
    }

    /**
     * The runtime section gives the warnings logged, the share of processor time spent collecting garbage, the live
     * data left after a collection and the open files, each with its limit when the server records one.
     */
    @Test
    @SuppressWarnings("unchecked")
    void verifyOverviewRuntimeMetrics() {
        val registry = new SimpleMeterRegistry();
        registry.counter("log4j2.events", "level", "warn").increment(3);
        Gauge.builder("jvm.gc.overhead", () -> .05).register(registry);
        Gauge.builder("jvm.gc.live.data.size", () -> 512).register(registry);
        Gauge.builder("jvm.gc.max.data.size", () -> 1024).register(registry);
        Gauge.builder("process.files.open", () -> 40).register(registry);
        val overview = new PalantirOverview(applicationContext,
            applicationContext.getBeanProvider(ServicesManager.class), applicationContext.getBeanProvider(TicketRegistry.class),
            new StaticListableBeanFactory(Map.of("meterRegistry", registry)).getBeanProvider(MeterRegistry.class),
            applicationContext.getBeanProvider(PalantirApplicationChanges.class));
        try {
            val sections = (Map<String, Object>) overview.summarize(Set.of("metrics"), true, Set.of("traffic")).get("sections");
            val traffic = (Map<String, Object>) sections.get("traffic");
            assertEquals(3.0, traffic.get("warningsLogged"));
            assertEquals(.05, traffic.get("gcOverhead"));
            assertEquals(Map.of("used", 512.0, "max", 1024.0), traffic.get("liveData"));
            assertEquals(Map.of("used", 40.0), traffic.get("files"));
        } finally {
            overview.destroy();
        }
    }

    /**
     * A section kept from earlier is shown until it expires, unless the summary is asked for fresh, as when the
     * operator refreshes the home page or one of its cards.
     */
    @Test
    @SuppressWarnings("unchecked")
    void verifyOverviewFreshSummarySkipsKeptSections() {
        val status = new AtomicReference<>(Health.up().build());
        val context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("memoryHealthIndicator", (HealthIndicator) status::get);
        context.refresh();
        val overview = new PalantirOverview(context,
            context.getBeanProvider(ServicesManager.class), context.getBeanProvider(TicketRegistry.class),
            context.getBeanProvider(MeterRegistry.class), context.getBeanProvider(PalantirApplicationChanges.class));
        try {
            final Function<Boolean, Object> health = fresh -> ((Map<String, Object>) ((Map<String, Object>) overview
                .summarize(Set.of("health"), true, Set.of("health"), fresh).get("sections")).get("health")).get("status");
            assertEquals("UP", health.apply(false));
            status.set(Health.down().build());
            assertEquals("UP", health.apply(false));
            assertEquals("DOWN", health.apply(true));
        } finally {
            overview.destroy();
            context.close();
        }
    }

    /**
     * A health check made of several checks, such as one per LDAP server being monitored, is listed once with the
     * worst status of its checks, in the order the health endpoint is told to use, where CAS puts WARN first; one with
     * no checks in it is left out.
     */
    @Test
    @SuppressWarnings("unchecked")
    void verifyOverviewHealthOfGroupedChecks() {
        val context = new StaticApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("health",
            Map.of("management.endpoint.health.status.order", "WARN,DOWN,OUT_OF_SERVICE,UNKNOWN,UP")));
        context.getBeanFactory().registerSingleton("warningHealthIndicator",
            CompositeHealthContributor.fromMap(Map.<String, HealthIndicator>of(
                "Primary", () -> Health.up().build(),
                "Secondary", () -> Health.status(new Status("WARN")).build())));
        context.getBeanFactory().registerSingleton("pooledLdapConnectionFactoryHealthIndicator",
            CompositeHealthContributor.fromMap(Map.<String, HealthIndicator>of(
                "Production LDAP", () -> Health.up().build(),
                "Backup LDAP", () -> Health.down().build())));
        context.getBeanFactory().registerSingleton("emptyHealthIndicator",
            CompositeHealthContributor.fromMap(Map.<String, HealthIndicator>of()));
        context.getBeanFactory().registerSingleton("memoryHealthIndicator", (HealthIndicator) () -> Health.up().build());
        context.refresh();
        val overview = new PalantirOverview(context,
            context.getBeanProvider(ServicesManager.class), context.getBeanProvider(TicketRegistry.class),
            context.getBeanProvider(MeterRegistry.class), context.getBeanProvider(PalantirApplicationChanges.class));
        try {
            val sections = (Map<String, Object>) overview.summarize(Set.of("health"), true, Set.of("health")).get("sections");
            val health = (Map<String, Object>) sections.get("health");
            val components = ((List<Map<String, Object>>) health.get("components")).stream()
                .collect(Collectors.toMap(component -> (String) component.get("name"), component -> component.get("status")));
            assertEquals(Map.of("pooledLdapConnectionFactory", "DOWN", "memory", "UP", "warning", "WARN"), components);
            assertEquals("WARN", health.get("status"));
            assertEquals(Boolean.TRUE, health.get("failing"));
            assertEquals("WARN", PalantirOverview.overallStatus(List.of("UP", "WARN"), List.of("WARN", "DOWN", "UP")));
            assertEquals("DOWN", PalantirOverview.overallStatus(List.of("UP", "WARN", "DOWN"), List.of("DOWN", "OUT_OF_SERVICE", "UP", "UNKNOWN")));
            assertEquals("UP", PalantirOverview.overallStatus(List.of("UP", "SLOW"), List.of("WARN", "DOWN", "OUT_OF_SERVICE", "UNKNOWN", "UP")));
            assertEquals("UP", PalantirOverview.overallStatus(List.of("SLOW"), List.of("DOWN", "UP")));
            assertEquals("UP", PalantirOverview.overallStatus(List.of(), List.of("DOWN", "UP")));
        } finally {
            overview.destroy();
            context.close();
        }
    }

    /**
     * Saving and deleting an application are remembered for the home page with who did it; internal CAS services
     * are left out, and a save repeated right away by another listener of the same change is counted once. A change
     * shows at once, even when the list was read just before it.
     */
    @Test
    @SuppressWarnings("unchecked")
    void verifyOverviewRemembersApplicationChanges() {
        val overview = applicationContext.getBean("palantirOverview", PalantirOverview.class);
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        service.setName("Overview Example");
        val internal = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        internal.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.INTERNAL_SERVICE_DEFINITION.getPropertyName(),
            new DefaultRegisteredServiceProperty("true"));
        overview.summarize(Set.of("registeredServices"), true);
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("casadmin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            overview.onApplicationSaved(new CasRegisteredServiceSavedEvent(this, service, ClientInfoHolder.getClientInfo()));
            overview.onApplicationSaved(new CasRegisteredServiceSavedEvent(this, service, ClientInfoHolder.getClientInfo()));
            overview.onApplicationSaved(new CasRegisteredServiceSavedEvent(this, internal, ClientInfoHolder.getClientInfo()));
        } finally {
            SecurityContextHolder.clearContext();
        }
        overview.onApplicationDeleted(new CasRegisteredServiceDeletedEvent(this, service, ClientInfoHolder.getClientInfo()));
        val kept = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        kept.setName("Overview Kept");
        val saved = Objects.requireNonNull(applicationContext.getBean(ServicesManager.BEAN_NAME, ServicesManager.class).save(kept));
        overview.onApplicationSaved(new CasRegisteredServiceSavedEvent(this, saved, ClientInfoHolder.getClientInfo()));

        val sections = (Map<String, Object>) overview.summarize(Set.of("registeredServices"), true).get("sections");
        val recent = (List<Map<String, Object>>) ((Map<String, Object>) sections.get("changes")).get("recent");
        val mine = recent.stream().filter(change -> Objects.equals(change.get("id"), String.valueOf(service.getId()))).toList();
        assertEquals(2, mine.size());
        assertEquals("deleted", mine.getFirst().get("action"));
        assertEquals("CAS", mine.getFirst().get("author"));
        assertEquals("saved", mine.getLast().get("action"));
        assertEquals("casadmin", mine.getLast().get("author"));
        assertEquals(Boolean.FALSE, mine.getFirst().get("exists"));
        assertTrue(recent.stream().anyMatch(change -> Objects.equals(change.get("id"), String.valueOf(saved.getId()))
            && Boolean.TRUE.equals(change.get("exists"))));
        assertTrue(recent.stream().noneMatch(change -> Objects.equals(change.get("id"), String.valueOf(internal.getId()))));
        assertEquals("node", ((Map<String, Object>) sections.get("changes")).get("scope"));
    }

    @Nested
    @TestPropertySource(properties = "cas.standalone.configuration-security.psw=Q7M9w4NjYnBxb2#mW")
    class ConfigurationEncryptionTests {
        @Test
        void verifyEncryptionIsAvailable() throws Throwable {
            val authentication = new UsernamePasswordAuthenticationToken("casuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            val model = mvc.perform(get("/palantir/dashboard").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getModel();
            assertEquals(Boolean.TRUE, model.get("configurationEncryptionAvailable"));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "management.endpoints.web.exposure.include=health,info",
        "management.endpoint.health.access=READ_ONLY",
        "management.endpoint.info.access=UNRESTRICTED"
    })
    class EndpointAccessTests {
        @Test
        void verifyAccessLevelsReachTheDashboard() throws Throwable {
            val authentication = new UsernamePasswordAuthenticationToken("casuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            val model = mvc.perform(get("/palantir/dashboard").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andReturn()
                .getModelAndView()
                .getModel();
            val access = (Map<?, ?>) model.get("actuatorEndpointAccess");
            assertNotNull(access);
            assertEquals("READ_ONLY", access.get("health"));
            assertEquals("UNRESTRICTED", access.get("info"));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "CasFeatureModule.Palantir.cas-authentication.enabled=true",
        "cas.palantir.cas-authentication.required-attribute-name=memberOf",
        "cas.palantir.cas-authentication.required-attribute-value=admin",
        "cas.monitor.endpoints.endpoint.info.access=AUTHENTICATED"
    })
    class CasAuthenticationTests {
        @Autowired
        @Qualifier("springSecurityFilterChain")
        private FilterChainProxy springSecurityFilterChain;

        @Test
        void verifyCasLoginIsLimitedToPalantir() throws Throwable {
            mvc.perform(get("/palantir/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/cas/login?service=**"));

            val entryPoint = springSecurityFilterChain.getFilterChains()
                .stream()
                .flatMap(chain -> chain.getFilters().stream())
                .filter(ExceptionTranslationFilter.class::isInstance)
                .map(ExceptionTranslationFilter.class::cast)
                .findFirst()
                .orElseThrow()
                .getAuthenticationEntryPoint();
            val request = new MockHttpServletRequest("GET", "/actuator/info");
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            val response = new MockHttpServletResponse();
            entryPoint.commence(request, response, new InsufficientAuthenticationException("none"));
            assertNull(response.getRedirectedUrl());
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
            assertTrue(String.valueOf(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Basic"));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.security.user.name=casadmin",
        "spring.security.user.password=Mellon",
        "spring.security.user.roles=ADMIN",
        "management.endpoints.web.exposure.include=health",
        "management.endpoint.health.show-components=when-authorized",
        "management.endpoint.health.roles=OPERATOR"
    })
    class SecurityTests {
        private static RequestPostProcessor browserSession() {
            return request -> {
                request.setRequestedSessionId("browser-session");
                request.setRequestedSessionIdFromCookie(true);
                return request;
            };
        }

        @Test
        void verifyLoginIgnoresRedirectTarget() throws Throwable {
            mvc.perform(post("/adminlogin")
                    .param("username", "casadmin")
                    .param("password", "Mellon")
                    .param("redirectTo", "https://attacker.example.org"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertFalse(String.valueOf(result.getResponse().getRedirectedUrl()).contains("attacker.example.org")));
        }

        @Test
        void verifyBrowserSessionWritesCarryToken() throws Throwable {
            mvc.perform(post("/palantir/dashboard/logout").with(browserSession()))
                .andExpect(status().isForbidden());
            mvc.perform(post("/actuator/health").with(browserSession()))
                .andExpect(status().isForbidden());
            mvc.perform(post("/palantir/dashboard/logout").with(browserSession()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        }

        @Test
        void verifyHealthChecksFollowTheHealthEndpoint() throws Throwable {
            val user = new UsernamePasswordAuthenticationToken("casuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
            mvc.perform(get("/palantir/dashboard/overview").param("sections", "health").with(authentication(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.health.status").exists())
                .andExpect(jsonPath("$.sections.health.components").doesNotExist());

            val operator = new UsernamePasswordAuthenticationToken("casoperator", "password",
                List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
            mvc.perform(get("/palantir/dashboard/overview").param("sections", "health").with(authentication(operator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.health.components").isArray());

            val administrator = new UsernamePasswordAuthenticationToken("casadmin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            mvc.perform(get("/palantir/dashboard/overview").param("sections", "health").with(authentication(administrator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.health.components").isArray());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "management.endpoints.web.base-path=/",
        "management.endpoints.web.exposure.include=health,info"
    })
    class RootActuatorTests {
        @Test
        void verifyBrowserSessionWritesToRootActuatorsCarryToken() throws Throwable {
            val browserSession = (RequestPostProcessor) request -> {
                request.setRequestedSessionId("browser-session");
                request.setRequestedSessionIdFromCookie(true);
                return request;
            };
            mvc.perform(post("/health").with(browserSession)).andExpect(status().isForbidden());
            mvc.perform(post("/info").with(browserSession)).andExpect(status().isForbidden());
            mvc.perform(post("/login").with(browserSession))
                .andExpect(result -> assertNotEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.palantir.templates-directory=${java.io.tmpdir}/palantir-shared-templates")
    class SharedTemplatesTests {
        private final Path directory = Path.of(System.getProperty("java.io.tmpdir"), "palantir-shared-templates");

        @BeforeEach
        void writeTemplates() throws Exception {
            Files.createDirectories(directory);
            Files.deleteIfExists(directory.resolve("portal.json"));
            Files.writeString(directory.resolve("intranet.json"), """
                {
                  "@class": "org.apereo.cas.services.CasRegisteredService",
                  "serviceId": "^https://intranet.example.org/.*",
                  "name": "Intranet",
                  "description": "Our intranet applications"
                }
                """);
            Files.writeString(directory.resolve("broken.json"), "{ \"@class\": \"org.apereo.cas.services.CasRegisteredService\", ");
            Files.writeString(directory.resolve("unknown.json"), """
                { "@class": "org.example.UnknownRegisteredService", "serviceId": "x", "name": "Unknown" }
                """);
            Files.writeString(directory.resolve("notes.txt"), "not a template");
        }

        @Test
        void verifySharedTemplatesAreOfferedAndUnreadableOnesSkipped() throws Throwable {
            val shared = serviceTemplates().stream().filter(template -> "shared".equals(template.get("source"))).toList();
            assertEquals(1, shared.size());
            assertEquals("org.apereo.cas.services.CasRegisteredService", shared.getFirst().get("type"));
            assertTrue(shared.getFirst().get("definition").contains("Our intranet applications"));

            Files.writeString(directory.resolve("portal.json"), """
                { "@class": "org.apereo.cas.services.CasRegisteredService", "serviceId": "^https://portal.example.org/.*", "name": "Portal" }
                """);
            assertEquals(2, serviceTemplates().stream().filter(template -> "shared".equals(template.get("source"))).count());

            val intranet = directory.resolve("intranet.json");
            Files.writeString(intranet, Files.readString(intranet).replace("Our intranet applications", "Every intranet application"));
            Files.setLastModifiedTime(intranet, FileTime.fromMillis(Files.getLastModifiedTime(intranet).toMillis() + 5000));
            assertTrue(serviceTemplates().stream().anyMatch(template -> template.get("definition").contains("Every intranet application")));
            assertEquals(2, serviceTemplates().stream().filter(template -> "shared".equals(template.get("source"))).count());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.palantir.templates-directory=/does/not/exist")
    class MissingSharedTemplatesTests {
        @Test
        void verifyMissingDirectoryKeepsBuiltInTemplates() throws Throwable {
            val templates = serviceTemplates();
            assertFalse(templates.isEmpty());
            assertTrue(templates.stream().allMatch(template -> "builtin".equals(template.get("source"))));
        }
    }
}
