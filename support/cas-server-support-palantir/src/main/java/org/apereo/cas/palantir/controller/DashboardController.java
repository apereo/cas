package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.palantir.PalantirAuthorities;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointAccessResolver;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
@RequiredArgsConstructor
@Tag(name = "Palantir")
@Slf4j
public class DashboardController extends AbstractController {
    /**
     * Registered service types are discovered with a full ClassGraph scan of the CAS namespace,
     * and the answer cannot change while the JVM is running. Holding it in a static supplier keeps
     * the scan off the request path, where it previously ran once per dashboard page view.
     */
    private static final Supplier<Map<String, String>> SUPPORTED_SERVICE_DEFINITIONS =
        memoize(DashboardController::scanSupportedServiceDefinitions);

    /**
     * Access strategies live in optional modules as well as in core, so the wizard offers only those whose module is
     * present; like the service types, the answer comes from a scan that runs once. When the scan fails, the set is
     * empty and the wizard offers every strategy it knows.
     */
    private static final Supplier<Set<String>> SUPPORTED_ACCESS_STRATEGIES =
        memoize(DashboardController::scanSupportedAccessStrategies);

    /**
     * Service properties are the constants of an enum; resolving them once avoids rebuilding the
     * same list on every render.
     */
    private static final Supplier<List<Map<String, String>>> SERVICE_PROPERTIES =
        memoize(() -> Arrays.stream(RegisteredServiceProperty.RegisteredServiceProperties.values())
            .map(property -> Map.of("name", property.getPropertyName(), "description", Objects.requireNonNullElse(property.getDescription(), StringUtils.EMPTY)))
            .toList());

    private final CasConfigurationProperties casProperties;
    private final EndpointLinksResolver endpointLinksResolver;
    private final WebEndpointProperties webEndpointProperties;
    private final ConfigurableApplicationContext applicationContext;
    private final List<? extends ExposableEndpoint<?>> endpoints;
    private final EndpointAccessResolver endpointAccessResolver;

    private final AtomicReference<SharedServiceTemplates> sharedServiceTemplates = new AtomicReference<>();

    private final Supplier<List<Map<String, String>>> builtInServiceTemplates =
        memoize(Unchecked.supplier(this::loadBuiltInServiceTemplates));

    /**
     * Dashboard home page explicitly defined.
     *
     * @param authentication who is signed in
     * @return the model and view
     * @throws Exception when the page cannot be put together
     */
    @GetMapping(path = {StringUtils.EMPTY, "/dashboard", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Dashboard home page", description = "Dashboard home page")
    public ModelAndView dashboardRoot(final Authentication authentication) throws Exception {
        return buildModelAndView(authentication);
    }

    /**
     * Tell the page whether its session is still signed in, and as whom. The session id itself is not given out: it
     * is the session, and scripts on the page have no use for it.
     *
     * @param request the request
     * @return the response entity
     */
    @GetMapping("/dashboard/session")
    @Operation(summary = "Get active session", description = "Gets active authenticated session")
    public ResponseEntity fetchSession(final HttpServletRequest request) {
        val auth = SecurityContextHolder.getContext().getAuthentication();
        val authenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        val session = request.getSession(false);

        if (!authenticated || session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("name", auth.getName()));
    }

    private ModelAndView buildModelAndView(final Authentication authentication) throws Exception {
        val mav = new ModelAndView("palantir/casPalantirDashboardView");
        mav.addObject("authentication", authentication);
        mav.addObject("casServerPrefix", casProperties.getServer().getPrefix());
        mav.addObject("clientInfo", ClientInfoHolder.getClientInfo());
        mav.addObject("isRoleAdmin", PalantirAuthorities.isAdministrator(authentication));

        val basePath = webEndpointProperties.getBasePath();
        val resolvedLinks = endpointLinksResolver.resolveLinks(basePath);
        val actuatorEndpoints = resolvedLinks
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), casProperties.getServer().getPrefix() + entry.getValue().getHref()))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        mav.addObject("actuatorEndpoints", actuatorEndpoints);
        mav.addObject("actuatorEndpointAccess", resolveEndpointAccess());
        mav.addObject("supportedServiceTypes", SUPPORTED_SERVICE_DEFINITIONS.get());
        mav.addObject("supportedAccessStrategies", SUPPORTED_ACCESS_STRATEGIES.get());
        mav.addObject("serviceTemplates", collectServiceTemplates());
        mav.addObject("availableMultifactorProviders", MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).keySet());
        mav.addObject("scriptFactoryAvailable", CasRuntimeHintsRegistrar.notInNativeImage()
            && ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory().isPresent());

        val mutablePropertySources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        mav.addObject("mutablePropertySources", mutablePropertySources.stream().map(MutablePropertySource::getName).toList());
        mav.addObject("configurationEncryptionAvailable", StringUtils.isNotBlank(applicationContext.getEnvironment()
            .getProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName())));
        mav.addObject("serviceProperties", SERVICE_PROPERTIES.get());
        return mav;
    }

    /**
     * The access level of every discovered endpoint, resolved the way the endpoint's mappings are, so that the
     * dashboard can hold back the requests a read-only endpoint refuses.
     *
     * @return the access level of each endpoint, by endpoint id
     */
    private Map<String, String> resolveEndpointAccess() {
        return endpoints
            .stream()
            .collect(Collectors.toMap(endpoint -> endpoint.getEndpointId().toString(),
                endpoint -> endpointAccessResolver.accessFor(endpoint.getEndpointId(), endpoint.getDefaultAccess()).name(),
                (first, second) -> first, TreeMap::new));
    }

    /**
     * Wrap a supplier so the delegate runs on first use and its result is reused afterwards.
     * <p>
     * Two callers racing on a cold context may each compute the value, which is harmless here
     * because every delegate is a pure scan of a fixed classpath; the point is that the scan leaves
     * the request path once a value has been published.
     *
     * @param delegate the supplier whose result is computed once and reused
     * @param <T>      the supplied type
     * @return a supplier that caches the delegate's first result
     */
    private static <T> Supplier<T> memoize(final Supplier<T> delegate) {
        val holder = new AtomicReference<T>();
        return () -> holder.updateAndGet(cached -> cached != null ? cached : delegate.get());
    }

    private static Set<String> scanSupportedAccessStrategies() {
        try {
            return ReflectionUtils.findSubclassesInPackage(RegisteredServiceAccessStrategy.class, CentralAuthenticationService.NAMESPACE)
                .stream()
                .filter(type -> !type.isInterface() && !type.isAnonymousClass() && !Modifier.isAbstract(type.getModifiers()))
                .map(Class::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        } catch (final Throwable e) {
            LOGGER.debug("Unable to find the access strategies of this deployment, so all are offered: [{}]", e.getMessage());
            return new TreeSet<>();
        }
    }

    private static Map<String, String> scanSupportedServiceDefinitions() {
        val subTypes = ReflectionUtils.findSubclassesInPackage(BaseRegisteredService.class, CentralAuthenticationService.NAMESPACE);
        return subTypes
            .stream()
            .filter(type -> !type.isInterface() && !type.isAnonymousClass()
                && !Modifier.isAbstract(type.getModifiers()) && !type.isMemberClass())
            .collect(Collectors.toMap(Class::getName, Unchecked.function(type -> {
                val service = (RegisteredService) type.getDeclaredConstructor().newInstance();
                return service.getFriendlyName();
            })));
    }

    private List<Map<String, String>> collectServiceTemplates() {
        val templates = new ArrayList<>(builtInServiceTemplates.get());
        val directory = casProperties.getPalantir().getTemplatesDirectory();
        if (StringUtils.isNotBlank(directory)) {
            templates.addAll(loadSharedServiceTemplates(Path.of(StringUtils.removeStart(directory, "file:"))));
        }
        return templates;
    }

    private List<Map<String, String>> loadBuiltInServiceTemplates() throws IOException {
        val resources = new PathMatchingResourcePatternResolver().getResources("classpath:service-definitions/**/*.json");
        val templates = new ArrayList<Map<String, String>>();
        for (val resource : resources) {
            try (val input = resource.getInputStream()) {
                val contents = new String(FileCopyUtils.copyToByteArray(input), StandardCharsets.UTF_8);
                toServiceTemplate(contents, "builtin").ifPresent(templates::add);
            }
        }
        return templates;
    }

    /**
     * The application templates of a shared directory, read again only when the directory changes: a template is added,
     * removed or edited, as the names, sizes and modification times of its files tell. Listing the directory costs far
     * less than reading and parsing every template on every view of the dashboard.
     *
     * @param directory the shared directory
     * @return the templates
     */
    private List<Map<String, String>> loadSharedServiceTemplates(final Path directory) {
        val fingerprint = fingerprintOf(directory);
        val cached = sharedServiceTemplates.get();
        if (cached != null && cached.directory().equals(directory) && cached.fingerprint().equals(fingerprint)) {
            return cached.templates();
        }
        val templates = readSharedServiceTemplates(directory);
        sharedServiceTemplates.set(new SharedServiceTemplates(directory, fingerprint, templates));
        return templates;
    }

    private static String fingerprintOf(final Path directory) {
        if (!Files.isDirectory(directory)) {
            return "missing";
        }
        try (val files = Files.list(directory)) {
            return files
                .filter(file -> Files.isRegularFile(file) && Strings.CI.endsWith(file.getFileName().toString(), ".json"))
                .sorted()
                .map(file -> {
                    try {
                        return file.getFileName() + ":" + Files.size(file) + ":" + Files.getLastModifiedTime(file).toMillis();
                    } catch (final IOException e) {
                        return file.getFileName() + ":unreadable";
                    }
                })
                .collect(Collectors.joining("|"));
        } catch (final IOException e) {
            return "unreadable:" + e.getMessage();
        }
    }

    private List<Map<String, String>> readSharedServiceTemplates(final Path directory) {
        if (!Files.isDirectory(directory)) {
            LOGGER.warn("Application templates directory [{}] does not exist or is not a directory", directory);
            return List.of();
        }
        try (val files = Files.list(directory)) {
            return files
                .filter(file -> Files.isRegularFile(file) && Strings.CI.endsWith(file.getFileName().toString(), ".json"))
                .sorted()
                .map(file -> {
                    try {
                        val template = toServiceTemplate(Files.readString(file, StandardCharsets.UTF_8), "shared");
                        if (template.isEmpty()) {
                            LOGGER.warn("Skipping application template [{}]: CAS cannot read it, or its application type is not available", file);
                        }
                        return template;
                    } catch (final Exception e) {
                        LOGGER.warn("Skipping application template [{}]: [{}]", file, e.getMessage());
                        return Optional.<Map<String, String>>empty();
                    }
                })
                .flatMap(Optional::stream)
                .toList();
        } catch (final IOException e) {
            LOGGER.warn("Unable to read application templates from [{}]: [{}]", directory, e.getMessage());
            return List.of();
        }
    }

    private Optional<Map<String, String>> toServiceTemplate(final String contents, final String source) {
        val serviceTypes = SUPPORTED_SERVICE_DEFINITIONS.get().keySet();
        if (serviceTypes.stream().noneMatch(contents::contains)) {
            return Optional.empty();
        }
        val definition = new RegisteredServiceJsonSerializer(applicationContext).from(contents);
        if (definition == null || !serviceTypes.contains(definition.getClass().getName())) {
            return Optional.empty();
        }
        return Optional.of(Map.of("source", source, "type", definition.getClass().getName(), "definition", contents));
    }

    private record SharedServiceTemplates(Path directory, String fingerprint, List<Map<String, String>> templates) {
    }
}
