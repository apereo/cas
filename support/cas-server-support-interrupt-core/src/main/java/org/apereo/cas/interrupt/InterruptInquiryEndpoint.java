package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link InterruptInquiryEndpoint}. It asks the interrupt inquirers what they produce for a person, optionally
 * signing in to an application, as the login flow asks them once the person has authenticated. The person is resolved
 * by the principal resolvers and no credential is verified. Every inquirer is asked, in order, and its response or
 * failure is reported along with the interrupt the login flow would show, which is the first response that interrupts.
 * Interrupt policy conditions of the registered service that only the login flow evaluates (principal attributes,
 * scripts and forced execution) are not evaluated here. Inquirers may call external endpoints or run scripts.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@Endpoint(id = "interrupts", defaultAccess = Access.NONE)
public class InterruptInquiryEndpoint extends BaseCasRestActuatorEndpoint {
    private static final PrincipalFactory PRINCIPAL_FACTORY = PrincipalFactoryUtils.newPrincipalFactory();

    private final ObjectProvider<InterruptInquiryExecutionPlan> interruptInquiryExecutionPlan;

    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    private final ObjectProvider<PrincipalResolver> principalResolver;

    public InterruptInquiryEndpoint(final CasConfigurationProperties casProperties,
                                    final ConfigurableApplicationContext applicationContext,
                                    final ObjectProvider<InterruptInquiryExecutionPlan> interruptInquiryExecutionPlan,
                                    final ObjectProvider<ServicesManager> servicesManager,
                                    final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
                                    final ObjectProvider<PrincipalResolver> principalResolver) {
        super(casProperties, applicationContext);
        this.interruptInquiryExecutionPlan = interruptInquiryExecutionPlan;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.principalResolver = principalResolver;
    }

    /**
     * Ask the interrupt inquirers what they produce for a person. A service URL that CAS refuses to build a service
     * from is a bad request, and a person the principal resolvers cannot resolve is not found.
     *
     * @param username the username of the person
     * @param service  the application URL the person signs in to, if any
     * @return the response of each inquirer and the interrupt the login flow would show, if any
     * @throws Throwable when the principal resolvers fail
     */
    @GetMapping(path = "{username}", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON
    })
    @Operation(summary = "Determine the interrupt payload produced for a user, optionally signing in to a service",
        parameters = {
            @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to inquire for"),
            @Parameter(name = "service", required = false, in = ParameterIn.QUERY,
                description = "The service the user signs in to, such as the service parameter sent to CAS at login")
        })
    public ResponseEntity<InterruptInquiryReport> inquire(
        @PathVariable final String username,
        @RequestParam(name = "service", required = false) final @Nullable String service)
            throws Throwable {
        try {
            return inquireFor(username, service);
        } catch (final UnauthorizedServiceException e) {
            LoggingUtils.warn(LOGGER, e);
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<InterruptInquiryReport> inquireFor(final String username, final @Nullable String service)
            throws Throwable {
        val selectedService = StringUtils.isNotBlank(service) ? webApplicationServiceFactory.getObject().createService(service) : null;
        if (StringUtils.isNotBlank(service) && selectedService == null) {
            return ResponseEntity.badRequest().build();
        }
        val registeredService = selectedService != null ? servicesManager.getObject().findServiceBy(selectedService) : null;
        val credential = new BasicIdentifiableCredential(username);
        val principal = principalResolver.getObject().resolve(credential,
            Optional.ofNullable(PRINCIPAL_FACTORY.createPrincipal(username)), Optional.empty(), Optional.ofNullable(selectedService));
        if (principal == null) {
            return ResponseEntity.notFound().build();
        }
        val authentication = DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
        val inquirers = interruptInquiryExecutionPlan.getObject().getInterruptInquirers()
            .stream()
            .map(inquirer -> inquireWith(inquirer, authentication, registeredService, selectedService, credential))
            .toList();
        val interrupt = inquirers
            .stream()
            .map(InquirerOutcome::response)
            .filter(Objects::nonNull)
            .filter(InterruptResponse::isInterrupt)
            .findFirst()
            .orElse(null);
        return ResponseEntity.ok(new InterruptInquiryReport(principal.getId(),
            selectedService != null ? selectedService.getId() : null,
            registeredService != null ? registeredService.getName() : null,
            inquirers, interrupt));
    }

    private static InquirerOutcome inquireWith(final InterruptInquirer inquirer, final Authentication authentication,
                                               final @Nullable RegisteredService registeredService,
                                               final @Nullable Service service, final Credential credential) {
        try {
            return new InquirerOutcome(inquirer.getName(), inquirer.inquire(authentication, registeredService, service, credential), null);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return new InquirerOutcome(inquirer.getName(), null,
                Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
        }
    }

    /**
     * What the interrupt inquirers produced for a person.
     *
     * @param principal         the resolved principal id
     * @param service           the service the person signs in to, if any
     * @param registeredService the name of the registered service that matches the service, if any
     * @param inquirers         the outcome of each inquirer, in order
     * @param interrupt         the interrupt the login flow would show, if any
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InterruptInquiryReport(String principal, @Nullable String service, @Nullable String registeredService,
                                         List<InquirerOutcome> inquirers, @Nullable InterruptResponse interrupt) {
    }

    /**
     * The outcome of asking one interrupt inquirer.
     *
     * @param name     the name of the inquirer
     * @param response the response of the inquirer, if it produced one
     * @param error    the reason the inquirer failed, if it did
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InquirerOutcome(String name, @Nullable InterruptResponse response, @Nullable String error) {
    }
}
