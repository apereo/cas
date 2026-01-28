package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
@RequiredArgsConstructor
@Tag(name = "Palantir")
public class DashboardController extends AbstractController {
    private final CasConfigurationProperties casProperties;
    private final EndpointLinksResolver endpointLinksResolver;
    private final WebEndpointProperties webEndpointProperties;
    private final ConfigurableApplicationContext applicationContext;

    /**
     * Dashboard home page explicitly defined.
     *
     * @return the model and view
     */
    @GetMapping(path = {StringUtils.EMPTY, "/dashboard", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Dashboard home page", description = "Dashboard home page")
    public ModelAndView dashboardRoot(final Authentication authentication, final HttpServletRequest request) throws Exception {
        return buildModelAndView(authentication, request);
    }

    /**
     * Fetch session response entity.
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
        return ResponseEntity.ok(Map.of(
            "name", auth.getName(),
            "id", session.getId()
        ));
    }

    /**
     * Logout.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping("/dashboard/logout")
    @Operation(summary = "Logout from the dashboard", description = "Logout from the dashboard")
    public ResponseEntity<Void> logout(final HttpServletRequest request, final HttpServletResponse response) {
        val auth = SecurityContextHolder.getContext().getAuthentication();
        val logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, auth);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private ModelAndView buildModelAndView(final Authentication authentication, final HttpServletRequest request) throws Exception {
        val mav = new ModelAndView("palantir/casPalantirDashboardView");
        mav.addObject("authentication", authentication);
        mav.addObject("casServerPrefix", casProperties.getServer().getPrefix());
        mav.addObject("httpRequestSecure", request.isSecure());
        mav.addObject("httpRequestMethod", request.getMethod());
        mav.addObject("httpRequestHeaders", HttpRequestUtils.getRequestHeaders(request));
        mav.addObject("clientInfo", ClientInfoHolder.getClientInfo());

        val basePath = webEndpointProperties.getBasePath();
        val endpoints = endpointLinksResolver.resolveLinks(basePath);
        val actuatorEndpoints = endpoints
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), casProperties.getServer().getPrefix() + entry.getValue().getHref()))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        mav.addObject("actuatorEndpoints", actuatorEndpoints);
        val serviceDefinitions = loadSupportedServiceDefinitions();
        mav.addObject("supportedServiceTypes", serviceDefinitions);
        mav.addObject("serviceDefinitions", loadExampleServiceDefinitions(serviceDefinitions.keySet()));
        mav.addObject("availableMultifactorProviders", MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).keySet());
        mav.addObject("scriptFactoryAvailable", CasRuntimeHintsRegistrar.notInNativeImage()
            && ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory().isPresent());

        val mutablePropertySources = CasCoreConfigurationUtils.getMutablePropertySources(applicationContext);
        mav.addObject("mutablePropertySources", mutablePropertySources.stream().map(MutablePropertySource::getName).toList());
        return mav;
    }

    private static Map<String, String> loadSupportedServiceDefinitions() {
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

    private Map<String, List<String>> loadExampleServiceDefinitions(final Set<String> serviceTypes) throws IOException {
        val jsonFilesMap = new HashMap<String, List<String>>();
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val resolver = new PathMatchingResourcePatternResolver();
        val resources = resolver.getResources("classpath:service-definitions/**/*.json");

        for (val resource : resources) {
            try (val input = resource.getInputStream()) {
                val contents = new String(FileCopyUtils.copyToByteArray(input), StandardCharsets.UTF_8);
                if (serviceTypes.stream().anyMatch(contents::contains)) {
                    val definition = serializer.from(contents);
                    if (definition != null) {
                        jsonFilesMap.computeIfAbsent(definition.getFriendlyName(), _ -> new ArrayList<>()).add(contents);
                    }
                }
            }
        }
        return jsonFilesMap;
    }
}
