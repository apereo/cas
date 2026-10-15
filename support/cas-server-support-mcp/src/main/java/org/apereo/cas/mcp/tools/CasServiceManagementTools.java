package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * This is {@link CasServiceManagementTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasServiceManagementTools {
    private static final int MAX_SERVICES = 50;

    private static final String REDACTED = "<redacted>";

    private static final Pattern SECRET_PROPERTY = Pattern.compile(
        "(?i).*(secrets?|password|apikey|accesskey|privatekey|secretkey|signingkey|encryptionkey)$");

    private static final String JWKS_PROPERTY = "jwks";

    private static final int MAX_ERROR_LENGTH = 500;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final RegisteredServiceParser registeredServiceSerializer;

    /**
     * Find a registered service and return its JSON definition.
     *
     * @param serviceId the service id
     * @return the registered service definition, or null if none is found
     */
    @McpTool(description = "Look up a registered service (application) by its numeric id and return its definition as JSON. "
        + "Client secrets, passwords, keys and inline key sets are replaced with " + REDACTED + ".",
        title = "Find Registered Service",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable String findService(
        @McpToolParam(description = "The numeric id of the registered service to look up")
        final long serviceId) {
        val registeredService = servicesManager.findServiceBy(serviceId);
        return registeredService != null ? toRedactedJson(registeredService) : null;
    }

    /**
     * Find the registered service that matches an application URL and return its JSON definition.
     *
     * @param service the application URL
     * @return the matching registered service definition, or null if no registration matches
     */
    @McpTool(description = "Find the registered service (application) that matches an application URL, "
        + "the way CAS does during login, and return its definition as JSON. "
        + "Client secrets, passwords, keys and inline key sets are replaced with " + REDACTED + ".",
        title = "Match Registered Service",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable String matchService(
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val webApplicationService = Objects.requireNonNull(serviceFactory.createService(service),
            () -> "Unable to build a service for " + service);
        val registeredService = servicesManager.findServiceBy(webApplicationService);
        return registeredService != null ? toRedactedJson(registeredService) : null;
    }

    /**
     * Search registered services by name, service id pattern, description or numeric id.
     *
     * @param query the search text
     * @return the number of matches and a summary of each
     */
    @McpTool(description = "Search registered services (applications) whose name, service id pattern, description or "
        + "numeric id contains the given text, ignoring case. At most " + MAX_SERVICES + " summaries are returned; "
        + "use findService with an id for the full definition.",
        title = "Search Registered Services",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public ServiceSearch searchServices(
        @McpToolParam(description = "The text to look for; an empty value matches every registered service")
        final String query) {
        val term = StringUtils.trimToEmpty(query);
        val matches = servicesManager.getAllServices()
            .stream()
            .filter(registeredService -> term.isEmpty()
                || String.valueOf(registeredService.getId()).equals(term)
                || Strings.CI.contains(registeredService.getName(), term)
                || Strings.CI.contains(registeredService.getServiceId(), term)
                || Strings.CI.contains(registeredService.getDescription(), term))
            .toList();
        return new ServiceSearch(term, matches.size(), matches.stream().limit(MAX_SERVICES).map(CasServiceManagementTools::summarize).toList());
    }

    /**
     * Validate a proposed registered service definition without saving it.
     *
     * @param definition the registered service definition as JSON
     * @param urls       application URLs to check the definition against, optional
     * @return the validation result
     */
    @McpTool(description = "Check a proposed registered service (application) definition, given as JSON, without saving it: "
        + "whether CAS can read it, whether its service id pattern is valid, whether its id or name collides with an existing "
        + "service, and for each given application URL whether the definition matches it, which existing service matches it "
        + "today and which of the two CAS would pick, by evaluation priority and order. The definition is not echoed back.",
        title = "Validate Service Definition",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public ServiceValidation validateServiceDefinition(
        @McpToolParam(description = "The registered service definition as JSON, including its @class")
        final String definition,
        @McpToolParam(description = "Application URLs to check the definition against, such as service parameters sent to CAS at login",
            required = false)
        @Nullable final List<String> urls) {
        val errors = new ArrayList<String>();
        val warnings = new ArrayList<String>();
        val candidate = parse(definition, errors);
        if (candidate == null) {
            return new ServiceValidation(false, null, errors, warnings, List.of());
        }
        if (StringUtils.isBlank(candidate.getServiceId())) {
            errors.add("The service id pattern is blank");
        } else {
            try {
                Pattern.compile(candidate.getServiceId());
            } catch (final PatternSyntaxException e) {
                errors.add("The service id pattern is not a valid regular expression: " + e.getDescription());
            }
        }
        if (StringUtils.isBlank(candidate.getName())) {
            warnings.add("The service has no name");
        }
        val existing = candidate.getId() > 0 ? servicesManager.findServiceBy(candidate.getId()) : null;
        if (existing != null) {
            warnings.add("Saving would replace the existing service %s (%s) with id %s".formatted(
                existing.getName(), existing.getServiceId(), existing.getId()));
        }
        servicesManager.getAllServices()
            .stream()
            .filter(service -> service.getId() != candidate.getId() && Strings.CI.equals(service.getName(), candidate.getName()))
            .forEach(service -> warnings.add("Another service with id %s already has the name %s".formatted(service.getId(), service.getName())));
        val checks = Objects.requireNonNullElseGet(urls, List::<String>of)
            .stream()
            .filter(StringUtils::isNotBlank)
            .map(url -> check(candidate, url))
            .toList();
        if (checks.isEmpty()) {
            warnings.add("No application URLs were given, so matching was not checked");
        } else if (checks.stream().noneMatch(UrlCheck::matches)) {
            warnings.add("The definition matches none of the given application URLs");
        }
        return new ServiceValidation(errors.isEmpty(), summarize(candidate), errors, warnings, checks);
    }

    private @Nullable RegisteredService parse(final String definition, final List<String> errors) {
        try {
            val service = registeredServiceSerializer.read(definition);
            if (service == null) {
                errors.add("The definition is empty");
            }
            return service;
        } catch (final Exception e) {
            errors.add("CAS cannot read the definition: " + StringUtils.abbreviate(
                Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()), MAX_ERROR_LENGTH));
            return null;
        }
    }

    private UrlCheck check(final RegisteredService candidate, final String url) {
        val matches = candidate.matches(url);
        val webApplicationService = serviceFactory.createService(url);
        val current = webApplicationService != null ? servicesManager.findServiceBy(webApplicationService) : null;
        val currentSummary = current != null ? summarize(current) : null;
        if (!matches) {
            return new UrlCheck(url, false, currentSummary, current != null ? Winner.EXISTING : Winner.NONE);
        }
        if (current == null || current.getId() == candidate.getId() || candidate.compareTo(current) < 0) {
            return new UrlCheck(url, true, currentSummary, Winner.CANDIDATE);
        }
        return new UrlCheck(url, true, currentSummary, Winner.EXISTING);
    }

    private String toRedactedJson(final RegisteredService registeredService) {
        val definition = MAPPER.readTree(registeredServiceSerializer.toString(registeredService));
        redact(definition);
        return MAPPER.writeValueAsString(definition);
    }

    private static void redact(final JsonNode node) {
        if (node instanceof final ObjectNode objectNode) {
            val names = objectNode.properties().stream().map(Map.Entry::getKey).toList();
            for (val name : names) {
                val value = objectNode.get(name);
                if (isSecret(name, value)) {
                    objectNode.put(name, REDACTED);
                } else {
                    redact(value);
                }
            }
        } else if (node.isArray()) {
            node.values().forEach(CasServiceManagementTools::redact);
        }
    }

    private static boolean isSecret(final String name, final JsonNode value) {
        if (value.isNull() || value.isBoolean() || value.isNumber() || (value.isString() && StringUtils.isBlank(value.asString()))) {
            return false;
        }
        if (SECRET_PROPERTY.matcher(name).matches()) {
            return true;
        }
        return JWKS_PROPERTY.equalsIgnoreCase(name) && value.isString()
            && !Strings.CI.startsWithAny(value.asString(), "http://", "https://", "file:", "classpath:");
    }

    private static ServiceSummary summarize(final RegisteredService registeredService) {
        return new ServiceSummary(registeredService.getId(), registeredService.getName(), registeredService.getServiceId(),
            registeredService.getFriendlyName(), registeredService.getEvaluationOrder());
    }

    /**
     * Who would handle an application URL if the proposed definition were saved.
     */
    public enum Winner {
        /**
         * The proposed definition.
         */
        CANDIDATE,
        /**
         * The registered service that matches today.
         */
        EXISTING,
        /**
         * No registered service.
         */
        NONE
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ServiceSummary(long id, @Nullable String name, @Nullable String serviceId, @Nullable String type, int evaluationOrder) {
    }

    public record ServiceSearch(String query, int count, List<ServiceSummary> services) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ServiceValidation(boolean valid, @Nullable ServiceSummary service, List<String> errors, List<String> warnings,
                                    List<UrlCheck> urls) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UrlCheck(String url, boolean matches, @Nullable ServiceSummary currentMatch, Winner winner) {
    }

    /**
     * Reads registered service JSON strictly, reporting why a definition cannot be read instead of logging it.
     */
    public static class RegisteredServiceParser extends RegisteredServiceJsonSerializer {
        @Serial
        private static final long serialVersionUID = 4316520462212410473L;

        public RegisteredServiceParser(final ConfigurableApplicationContext applicationContext) {
            super(applicationContext);
        }

        /**
         * Read a registered service definition.
         *
         * @param definition the definition
         * @return the registered service
         */
        public @Nullable RegisteredService read(final String definition) {
            return getTypeReader().readValue(definition);
        }
    }
}
