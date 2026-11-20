package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.LogicalOperatorTypes;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.apereo.cas.services.util.RegisteredServiceAccessStrategyEvaluator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import org.apereo.cas.web.support.ArgumentExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * This is {@link RegisteredServiceAccessEndpoint}. It decides whether a user may access an application, as CAS would
 * at sign-in, and explains the decision as the steps CAS goes through, whether access is allowed or refused.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Endpoint(id = "serviceAccess", defaultAccess = Access.NONE)
@Slf4j
public class RegisteredServiceAccessEndpoint extends BaseCasRestActuatorEndpoint {
    private static final String PARAMETER_CLIENT_ID = "client_id";

    private static final String PARAMETER_ENTITY_ID = "entityId";

    /**
     * Where CAS receives OAuth and OpenID Connect requests on its own behalf, so a request there carrying a client id
     * is matched to that client.
     */
    private static final String OAUTH_CALLBACK_PATH = "/oauth2.0/callbackAuthorize";

    private final ObjectProvider<ServicesManager> servicesManager;
    private final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;
    private final ObjectProvider<ArgumentExtractor> argumentExtractor;
    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;
    private final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;
    private final ObjectProvider<PrincipalResolver> principalResolver;
    private final ObjectProvider<PrincipalFactory> principalFactory;

    public RegisteredServiceAccessEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan,
        final ObjectProvider<ArgumentExtractor> argumentExtractor,
        final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
        final ObjectProvider<PrincipalResolver> principalResolver,
        final ObjectProvider<PrincipalFactory> principalFactory) {
        super(casProperties, applicationContext);
        this.authenticationServiceSelectionPlan = authenticationServiceSelectionPlan;
        this.servicesManager = servicesManager;
        this.argumentExtractor = argumentExtractor;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.principalResolver = principalResolver;
        this.principalFactory = principalFactory;
    }
    
    /**
     * Authorize user based on application access strategy.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping(
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Verify if service access can be granted to the user, and explain the decision as the steps CAS goes through",
        parameters = {
            @Parameter(name = "username", required = true, in = ParameterIn.QUERY, description = "The username to authenticate"),
            @Parameter(name = "password", required = false, in = ParameterIn.QUERY, description = "The password to authenticate the user if necessary"),
            @Parameter(name = "service", required = false, in = ParameterIn.QUERY, description = "The service to authorize access to"),
            @Parameter(name = "client_id", required = false, in = ParameterIn.QUERY, description = "The application client id for OAuth or OpenID Connect"),
            @Parameter(name = "entityId", required = false, in = ParameterIn.QUERY, description = "The entity id for SAML2 service providers")
        })
    public ResponseEntity authorize(final HttpServletRequest request) {
        val steps = new ArrayList<AccessStep>();
        Service service = null;
        RegisteredService registeredService = null;
        Authentication authentication = null;
        try {
            service = authenticationServiceSelectionPlan.getObject().resolveService(extractService(request));
            registeredService = service == null ? null : servicesManager.getObject().findServiceBy(service);
            if (registeredService != null && namedByIdentifier(request)
                && CasRegisteredService.FRIENDLY_NAME.equalsIgnoreCase(registeredService.getFriendlyName())) {
                registeredService = null;
            }
            if (service == null || registeredService == null) {
                val reason = service == null ? "No application was named" : "No application matches %s".formatted(requested(request, service));
                steps.add(AccessStep.of("application", "Application found", false, reason));
                return explanation(HttpStatus.FORBIDDEN, reason, steps, service, null, null);
            }
            steps.add(AccessStep.of("application", "Application found", true,
                "%s (#%s) matches %s".formatted(registeredService.getName(), registeredService.getId(), service.getId())));
            steps.addAll(AccessExplainer.explainApplication(registeredService, service));
            if (steps.stream().anyMatch(AccessStep::failed)) {
                return explanation(HttpStatus.FORBIDDEN, firstFailure(steps), steps, service, registeredService, null);
            }

            val password = request.getParameter("password");
            try {
                authentication = buildAuthentication(request.getParameter("username"), password, service);
            } catch (final AuthenticationException e) {
                LoggingUtils.warn(LOGGER, e);
                steps.add(AccessStep.of("authentication", "Signed in", false, "The username or password is not accepted"));
                return explanation(HttpStatus.UNAUTHORIZED, firstFailure(steps), steps, service, registeredService, null);
            }
            steps.add(AccessStep.of("authentication", "Signed in", true,
                StringUtils.isNotBlank(password) ? "The password is accepted" : "Attributes are resolved without checking a password"));

            val attributes = new LinkedHashMap<String, List<Object>>(authentication.getAttributes());
            attributes.putAll(authentication.getPrincipal().getAttributes());
            val accessRequest = RegisteredServiceAccessStrategyRequest.builder()
                .service(service)
                .registeredService(registeredService)
                .principalId(authentication.getPrincipal().getId())
                .attributes(attributes)
                .applicationContext(applicationContext)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build());
            steps.addAll(explainAuthorization(registeredService, accessRequest, !accessResult.isExecutionFailure()));
            if (accessResult.isExecutionFailure() && steps.stream().noneMatch(AccessStep::failed)) {
                steps.add(AccessStep.of("enforcement", "Other access rules", false,
                    accessResult.getException().map(Throwable::getMessage).orElse("Access is refused")));
            }
            return accessResult.isExecutionFailure()
                ? explanation(HttpStatus.FORBIDDEN, firstFailure(steps), steps, service, registeredService, authentication)
                : explanation(HttpStatus.OK, "%s may access %s".formatted(authentication.getPrincipal().getId(), registeredService.getName()),
                    steps, service, registeredService, authentication);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            steps.add(AccessStep.of("error", "Decision", false, StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName())));
            return explanation(HttpStatus.INTERNAL_SERVER_ERROR, "Access could not be checked: " + firstFailure(steps),
                steps, service, registeredService, authentication);
        }
    }

    /**
     * The steps of the access strategy for the decision CAS made. The decision stands when its rules cannot be
     * explained, such as when a script among them fails; the explanation then says so.
     *
     * @param registeredService the application
     * @param accessRequest     the user and the application
     * @param allowed           whether CAS allowed access
     * @return the steps
     */
    private static List<AccessStep> explainAuthorization(final RegisteredService registeredService,
                                                         final RegisteredServiceAccessStrategyRequest accessRequest,
                                                         final boolean allowed) {
        try {
            return AccessExplainer.explainAuthorization(registeredService.getAccessStrategy(), accessRequest, allowed);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return List.of(AccessStep.of("strategy", "Access strategy", allowed ? Outcome.PASSED : Outcome.FAILED,
                "%s; its rules could not be explained: %s".formatted(allowed ? "Allows access" : "Refuses access",
                    StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()))));
        }
    }

    /**
     * Whether the request names its application by a client id or an entity id alone. Only an OAuth, OpenID Connect or
     * SAML2 application answers it, never a CAS application whose service id happens to match the URL read from it.
     *
     * @param request the request
     * @return true when the request has no service or target service URL but a client id or an entity id
     */
    private static boolean namedByIdentifier(final HttpServletRequest request) {
        return StringUtils.isAllBlank(request.getParameter(CasProtocolConstants.PARAMETER_SERVICE),
                request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE))
            && (StringUtils.isNotBlank(request.getParameter(PARAMETER_CLIENT_ID)) || StringUtils.isNotBlank(request.getParameter(PARAMETER_ENTITY_ID)));
    }

    /**
     * What the request names, for people: the service URL, or the client id or entity id it was given alone.
     *
     * @param request the request
     * @param service the service read from it
     * @return the description
     */
    private static String requested(final HttpServletRequest request, final Service service) {
        if (namedByIdentifier(request)) {
            if (StringUtils.isNotBlank(request.getParameter(PARAMETER_CLIENT_ID))) {
                return "client id " + request.getParameter(PARAMETER_CLIENT_ID);
            }
            if (StringUtils.isNotBlank(request.getParameter(PARAMETER_ENTITY_ID))) {
                return "entity id " + request.getParameter(PARAMETER_ENTITY_ID);
            }
        }
        return service.getId();
    }

    private static String firstFailure(final List<AccessStep> steps) {
        return steps.stream()
            .flatMap(RegisteredServiceAccessEndpoint::flatten)
            .filter(step -> step.failed() && step.steps().stream().noneMatch(AccessStep::failed))
            .findFirst()
            .map(RegisteredServiceAccessEndpoint::describeFailure)
            .orElse("Access is refused");
    }

    private static String describeFailure(final AccessStep step) {
        val rejected = "rejected".equals(step.id());
        val rules = step.rules()
            .stream()
            .filter(rule -> rejected == rule.matched())
            .map(rule -> "%s %s %s, and has %s".formatted(rule.attribute(), rejected ? "is refused for" : "needs",
                String.join(", ", rule.values()), rule.actual().isEmpty() ? "none" : rule.actual().stream().map(String::valueOf).collect(Collectors.joining(", "))))
            .toList();
        return rules.isEmpty()
            ? "%s: %s".formatted(step.title(), step.detail())
            : "%s: %s".formatted(step.title(), String.join("; ", rules));
    }

    private static Stream<AccessStep> flatten(final AccessStep step) {
        val inner = step.failed() ? step.steps().stream().flatMap(RegisteredServiceAccessEndpoint::flatten) : Stream.<AccessStep>empty();
        return Stream.concat(Stream.of(step), inner);
    }

    private static ResponseEntity<Map<String, Object>> explanation(final HttpStatus status, final String reason,
                                                                   final List<AccessStep> steps,
                                                                   @Nullable final Service service,
                                                                   @Nullable final RegisteredService registeredService,
                                                                   @Nullable final Authentication authentication) {
        val body = new LinkedHashMap<String, Object>();
        body.put("allowed", status == HttpStatus.OK);
        body.put("reason", reason);
        body.put("steps", steps);
        body.put("service", service == null ? null : Map.of("id", service.getId()));
        body.put("registeredService", summaryOf(registeredService));
        body.put("authentication", authentication);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * What a check tells of the application: enough to name and find it, without its definition, which can hold keys,
     * secrets and the addresses of the services it talks to; the definition is for the registered services endpoint.
     *
     * @param registeredService the application
     * @return the summary, or null when there is no application
     */
    private static @Nullable Map<String, Object> summaryOf(@Nullable final RegisteredService registeredService) {
        if (registeredService == null) {
            return null;
        }
        val summary = new LinkedHashMap<String, Object>();
        summary.put("@class", registeredService.getClass().getName());
        summary.put("id", registeredService.getId());
        summary.put("name", registeredService.getName());
        summary.put("serviceId", registeredService.getServiceId());
        summary.put("evaluationOrder", registeredService.getEvaluationOrder());
        return summary;
    }

    /**
     * The service the request names. A request with only a client id, for an OAuth or OpenID Connect application, or
     * only an entity id, for a SAML2 service provider, is read as the request CAS receives for them: one addressed to
     * its OAuth callback that carries the client id, or one for the entity id.
     *
     * @param request the request
     * @return the service, or null when the request names none
     */
    private @Nullable Service extractService(final HttpServletRequest request) {
        val extractor = argumentExtractor.getObject();
        val service = extractor.extractService(request);
        if (service != null) {
            return service;
        }
        val clientId = request.getParameter(PARAMETER_CLIENT_ID);
        val entityId = request.getParameter(PARAMETER_ENTITY_ID);
        val target = StringUtils.isNotBlank(clientId)
            ? casProperties.getServer().getPrefix() + OAUTH_CALLBACK_PATH
            : entityId;
        return Optional.ofNullable(target)
            .filter(StringUtils::isNotBlank)
            .map(value -> extractor.extractService(withServiceParameter(request, value)))
            .orElse(null);
    }

    private static HttpServletRequest withServiceParameter(final HttpServletRequest request, final String serviceId) {
        val parameters = new LinkedHashMap<String, String[]>(request.getParameterMap());
        parameters.put(CasProtocolConstants.PARAMETER_SERVICE, new String[]{serviceId});
        return new HttpServletRequestWrapper(request) {
            @Override
            public @Nullable String getParameter(final String name) {
                return Optional.ofNullable(parameters.get(name)).filter(values -> values.length > 0).map(values -> values[0]).orElse(null);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return parameters;
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(parameters.keySet());
            }

            @Override
            public String @Nullable [] getParameterValues(final String name) {
                return parameters.get(name);
            }
        };
    }

    private Authentication buildAuthentication(final String username, final String password,
                                               final Service selectedService)
            throws Throwable {
        if (StringUtils.isNotBlank(password)) {
            val credential = new UsernamePasswordCredential(username, password);
            val result = authenticationSystemSupport.getObject().finalizeAuthenticationTransaction(selectedService, credential);
            return result.getAuthentication();
        }
        val principal = principalResolver.getObject().resolve(new BasicIdentifiableCredential(username),
            Optional.of(principalFactory.getObject().createPrincipal(username)),
            Optional.empty(), Optional.of(selectedService));
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

    /**
     * How a step turned out.
     */
    private enum Outcome {
        /**
         * The step lets the user through.
         */
        PASSED,
        /**
         * The step refuses access.
         */
        FAILED,
        /**
         * The step was not checked.
         */
        SKIPPED,
        /**
         * The step is reported without deciding access.
         */
        INFO
    }

    /**
     * One attribute rule of a step: the values the rule names, the values the user has, and whether they match.
     *
     * @param attribute the attribute
     * @param values    the values the rule names
     * @param actual    the values the user has
     * @param matched   whether the user's values match the rule
     */
    private record Rule(String attribute, Set<String> values, List<Object> actual, boolean matched) {
    }

    /**
     * One step of the decision on whether a user may access an application, as the service access endpoint reports
     * it: what was checked, how it turned out and why, with the attribute rules it compared and the steps it is made of,
     * such as the strategies of a chain.
     *
     * @param id      the kind of step, such as {@code required} for the required attributes
     * @param title   the step's name
     * @param outcome how the step turned out
     * @param detail  why, in a sentence
     * @param rules   the attribute rules the step compared
     * @param steps   the steps it is made of
     */
    private record AccessStep(String id, String title, Outcome outcome, String detail,
                              List<Rule> rules, List<AccessStep> steps) {

        /**
         * A step with no rules or inner steps.
         *
         * @param id      the kind of step
         * @param title   the step's name
         * @param outcome how it turned out
         * @param detail  why
         * @return the step
         */
        static AccessStep of(final String id, final String title, final Outcome outcome, final String detail) {
            return new AccessStep(id, title, outcome, detail, List.of(), List.of());
        }

        /**
         * A step that lets the user through or refuses access.
         *
         * @param id      the kind of step
         * @param title   the step's name
         * @param passed  whether it lets the user through
         * @param detail  why
         * @return the step
         */
        static AccessStep of(final String id, final String title, final boolean passed, final String detail) {
            return of(id, title, passed ? Outcome.PASSED : Outcome.FAILED, detail);
        }

        /**
         * Whether the step refuses access.
         *
         * @return true when it failed
         */
        boolean failed() {
            return outcome == Outcome.FAILED;
        }
    }

    /**
     * Explains the decision on whether a user may access an application as the steps CAS goes through: whether the
     * application takes sign-ins, has not expired and joins single sign-on, and, for the user, the access strategy's
     * activation and its required and rejected attributes, one attribute at a time. Strategies that decide by other
     * means, such as an external authorization service or a script, are not asked again: they are reported with the
     * decision CAS made, and a chain of strategies as a step made of the steps of each.
     */
    private static final class AccessExplainer {
        private AccessExplainer() {
        }

        /**
         * The steps that do not depend on the user: whether the application takes sign-ins, or does so within its
         * window, whether it has expired, and whether it joins single sign-on.
         *
         * @param registeredService the application
         * @param service           the service requested
         * @return the steps
         */
        static List<AccessStep> explainApplication(final RegisteredService registeredService, final Service service) {
            val strategy = registeredService.getAccessStrategy();
            val allowed = strategy.isServiceAccessAllowed(registeredService, service);
            val availability = strategy instanceof final TimeBasedRegisteredServiceAccessStrategy timeBased
                ? AccessStep.of("window", "Active window", allowed,
                    "%s, from %s until %s (%s)".formatted(allowed ? "Within its active window" : "Outside its active window",
                        StringUtils.defaultIfBlank(timeBased.getStartingDateTime(), "any time"),
                        StringUtils.defaultIfBlank(timeBased.getEndingDateTime(), "any time"),
                        StringUtils.defaultIfBlank(timeBased.getZoneId(), "the time zone of the server")))
                : AccessStep.of("enabled", "Enabled", allowed,
                    allowed ? "The application accepts sign-ins" : "The application is disabled");

            val expirationDate = Optional.ofNullable(registeredService.getExpirationPolicy())
                .map(RegisteredServiceExpirationPolicy::getExpirationDate)
                .filter(StringUtils::isNotBlank);
            val notExpired = RegisteredServiceAccessStrategyUtils.ensureServiceIsNotExpired(registeredService);
            val expiration = AccessStep.of("expiration", "Not expired", notExpired,
                expirationDate.map(date -> (notExpired ? "Expires on " : "Expired on ") + date).orElse("No expiration date is set"));

            val sso = strategy.isServiceAccessAllowedForSso(registeredService);
            val singleSignOn = AccessStep.of("sso", "Single sign-on", Outcome.INFO,
                sso ? "An existing single sign-on session is reused" : "Every sign-in asks for credentials again");
            return List.of(availability, expiration, singleSignOn);
        }

        /**
         * The steps that decide for the user: the access strategy's activation and attribute rules, the steps of each
         * strategy of a chain, or the answer of a strategy that decides by other means. The attribute rules are evaluated
         * here, since that only compares values; a strategy that asks an outside service or runs a script is not asked
         * again, and its answer is the decision CAS already made.
         *
         * @param strategy the access strategy
         * @param request  the user and the application
         * @param allowed  whether CAS allowed access when it decided
         * @return the steps
         * @throws Throwable when the attribute rules cannot be evaluated
         */
        static List<AccessStep> explainAuthorization(final RegisteredServiceAccessStrategy strategy,
                                                     final RegisteredServiceAccessStrategyRequest request,
                                                     final boolean allowed)
                throws Throwable {
            if (strategy instanceof ChainingRegisteredServiceAccessStrategy) {
                return List.of(explainStrategy(strategy, request, allowed));
            }
            if (explainsAttributes(strategy)) {
                return explainDefault((DefaultRegisteredServiceAccessStrategy) strategy, request);
            }
            if (deciderOf(strategy) == RegisteredServiceAccessStrategy.class) {
                return List.of(AccessStep.of("strategy", "User rules", Outcome.SKIPPED,
                    "%s has no rules about the user".formatted(nameOf(strategy))));
            }
            return List.of(new AccessStep("strategy", "Decided by " + nameOf(strategy), outcomeOf(allowed),
                "%s %s".formatted(nameOf(strategy), allowed ? "allows access" : "refuses access"), List.of(), List.of()));
        }

        /**
         * One strategy as a step: a chain with a step for each of its strategies, attribute rules with their steps, and a
         * strategy that decides by other means with the decision CAS made, when it is the one that decided, or else as not
         * asked again.
         *
         * @param strategy the strategy
         * @param request  the user and the application
         * @param decided  whether CAS allowed access, when this strategy alone decided, or null for one strategy of a chain
         * @return the step
         * @throws Throwable when the attribute rules cannot be evaluated
         */
        private static AccessStep explainStrategy(final RegisteredServiceAccessStrategy strategy,
                                                  final RegisteredServiceAccessStrategyRequest request,
                                                  @Nullable final Boolean decided)
                throws Throwable {
            if (strategy instanceof final ChainingRegisteredServiceAccessStrategy chain) {
                val inner = new ArrayList<AccessStep>();
                for (val member : chain.getStrategies()) {
                    inner.add(explainStrategy(member, request, null));
                }
                val any = chain.getOperator() == LogicalOperatorTypes.OR;
                val outcome = chainOutcome(any, inner)
                    .or(() -> Optional.ofNullable(decided).map(AccessExplainer::outcomeOf))
                    .orElse(Outcome.INFO);
                return new AccessStep("chain", "Chained strategies", outcome,
                    "%s of %d strategies must allow access".formatted(any ? "Any one" : "Every one", inner.size()), List.of(), inner);
            }
            if (explainsAttributes(strategy)) {
                val allowed = strategy.authorizeRequest(request);
                return new AccessStep("strategy", nameOf(strategy), outcomeOf(allowed), allowed ? "Allows access" : "Refuses access",
                    List.of(), explainDefault((DefaultRegisteredServiceAccessStrategy) strategy, request));
            }
            if (deciderOf(strategy) == RegisteredServiceAccessStrategy.class) {
                return AccessStep.of("strategy", nameOf(strategy), Outcome.PASSED, "Has no rules about the user");
            }
            return decided == null
                ? AccessStep.of("strategy", nameOf(strategy), Outcome.INFO,
                    "Not asked again: CAS asks it once, when it decides")
                : AccessStep.of("strategy", nameOf(strategy), decided, decided ? "Allows access" : "Refuses access");
        }

        /**
         * How a chain turns out from the steps of its strategies, when they settle it: any one allowing access, or every
         * one refusing it, for a chain that needs any one; and any one refusing access, or every one allowing it, for a
         * chain that needs every one.
         *
         * @param any   whether any one strategy is enough
         * @param inner the steps of the strategies
         * @return the outcome, or nothing when a strategy that was not asked again would settle it
         */
        private static Optional<Outcome> chainOutcome(final boolean any, final List<AccessStep> inner) {
            val wanted = any ? Outcome.PASSED : Outcome.FAILED;
            if (inner.stream().anyMatch(step -> step.outcome() == wanted)) {
                return Optional.of(wanted);
            }
            val other = any ? Outcome.FAILED : Outcome.PASSED;
            return inner.stream().allMatch(step -> step.outcome() == other) ? Optional.of(other) : Optional.empty();
        }

        private static Outcome outcomeOf(final boolean passed) {
            return passed ? Outcome.PASSED : Outcome.FAILED;
        }

        /**
         * Whether the strategy's rules can be evaluated here without running anything again: the default strategy, as
         * long as no script decides its activation or any of its attribute values.
         *
         * @param strategy the strategy
         * @return true when its attribute rules are explained one by one
         */
        private static boolean explainsAttributes(final RegisteredServiceAccessStrategy strategy) {
            return strategy instanceof final DefaultRegisteredServiceAccessStrategy defaultStrategy
                && deciderOf(strategy) == DefaultRegisteredServiceAccessStrategy.class
                && !runsScripts(defaultStrategy);
        }

        private static boolean runsScripts(final DefaultRegisteredServiceAccessStrategy strategy) {
            val values = Stream.concat(strategy.getRequiredAttributes().values().stream(), strategy.getRejectedAttributes().values().stream())
                .flatMap(Collection::stream);
            val scripts = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
            return runsScripts(strategy.getActivationCriteria())
                || (scripts.isPresent() && values.anyMatch(value -> scripts.get().isInlineScript(value)));
        }

        private static boolean runsScripts(@Nullable final RegisteredServiceAccessStrategyActivationCriteria criteria) {
            if (criteria == null || criteria instanceof AttributeBasedRegisteredServiceAccessStrategyActivationCriteria) {
                return false;
            }
            if (criteria instanceof final ChainingRegisteredServiceAccessStrategyActivationCriteria chain) {
                return chain.getConditions().stream().anyMatch(AccessExplainer::runsScripts);
            }
            return true;
        }

        private static Class<?> deciderOf(final RegisteredServiceAccessStrategy strategy) {
            try {
                return strategy.getClass().getMethod("authorizeRequest", RegisteredServiceAccessStrategyRequest.class).getDeclaringClass();
            } catch (final NoSuchMethodException e) {
                return strategy.getClass();
            }
        }

        private static List<AccessStep> explainDefault(final DefaultRegisteredServiceAccessStrategy strategy,
                                                       final RegisteredServiceAccessStrategyRequest request)
                throws Throwable {
            val steps = new ArrayList<AccessStep>();
            val criteria = strategy.getActivationCriteria();
            if (criteria != null) {
                if (!criteria.shouldActivate(request)) {
                    steps.add(AccessStep.of("activation", "Activation", criteria.isAllowIfInactive(),
                        "The attribute rules do not apply to this user, so access is %s".formatted(criteria.isAllowIfInactive() ? "allowed" : "refused")));
                    steps.add(AccessStep.of("required", "Required attributes", Outcome.SKIPPED, "Not checked: the rules do not apply"));
                    steps.add(AccessStep.of("rejected", "Rejected attributes", Outcome.SKIPPED, "Not checked: the rules do not apply"));
                    return steps;
                }
                steps.add(AccessStep.of("activation", "Activation", Outcome.PASSED, "The attribute rules apply to this user"));
            }

            val required = strategy.getRequiredAttributes();
            if (required.isEmpty()) {
                steps.add(AccessStep.of("required", "Required attributes", Outcome.SKIPPED, "None are defined"));
            } else {
                val rules = rulesOf(strategy, required, request, true);
                val passed = evaluator(strategy, required, Map.of()).apply(request);
                steps.add(new AccessStep("required", "Required attributes", passed ? Outcome.PASSED : Outcome.FAILED,
                    strategy.isRequireAllAttributes() ? "Every one of these must match" : "Any one of these must match", rules, List.of()));
            }

            val rejected = strategy.getRejectedAttributes();
            if (rejected.isEmpty()) {
                steps.add(AccessStep.of("rejected", "Rejected attributes", Outcome.SKIPPED, "None are defined"));
            } else {
                val rules = rulesOf(strategy, rejected, request, false);
                val passed = evaluator(strategy, Map.of(), rejected).apply(request);
                steps.add(new AccessStep("rejected", "Rejected attributes", passed ? Outcome.PASSED : Outcome.FAILED,
                    strategy.isRequireAllAttributes() ? "Access is refused when every one of these matches" : "Access is refused when any one of these matches",
                    rules, List.of()));
            }
            return steps;
        }

        private static List<Rule> rulesOf(final DefaultRegisteredServiceAccessStrategy strategy, final Map<String, Set<String>> attributes,
                                          final RegisteredServiceAccessStrategyRequest request, final boolean required) {
            return attributes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    val single = Map.of(entry.getKey(), entry.getValue());
                    val matched = required
                        ? evaluator(strategy, single, Map.of()).apply(request)
                        : !evaluator(strategy, Map.of(), single).apply(request);
                    val actual = Optional.ofNullable(request.getAttributes().get(entry.getKey())).orElseGet(List::of);
                    return new Rule(entry.getKey(), new TreeSet<>(entry.getValue()), actual, matched);
                })
                .toList();
        }

        private static RegisteredServiceAccessStrategyEvaluator evaluator(final DefaultRegisteredServiceAccessStrategy strategy,
                                                                          final Map<String, Set<String>> required,
                                                                          final Map<String, Set<String>> rejected) {
            return RegisteredServiceAccessStrategyEvaluator.builder()
                .caseInsensitive(strategy.isCaseInsensitive())
                .requireAllAttributes(strategy.isRequireAllAttributes())
                .requiredAttributes(required)
                .rejectedAttributes(rejected)
                .build();
        }

        /**
         * The name of a strategy for people, such as Open Policy Agent for the Open Policy Agent access strategy.
         *
         * @param strategy the strategy
         * @return the name
         */
        static String nameOf(final RegisteredServiceAccessStrategy strategy) {
            val name = Strings.CS.removeEnd(Strings.CS.removeEnd(strategy.getClass().getSimpleName(), "AccessStrategy"), "RegisteredService");
            return String.join(" ", StringUtils.splitByCharacterTypeCamelCase(StringUtils.defaultIfBlank(name, "Access")));
        }
    }
}
