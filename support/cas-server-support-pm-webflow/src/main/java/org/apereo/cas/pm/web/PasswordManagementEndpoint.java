package org.apereo.cas.pm.web;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link PasswordManagementEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Endpoint(id = "passwordManagement", defaultAccess = Access.NONE)
@Slf4j
public class PasswordManagementEndpoint extends BaseCasRestActuatorEndpoint {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    /**
     * The communication manager for SMS/emails.
     */
    protected final ObjectProvider<CommunicationsManager> communicationsManager;

    /**
     * The password management service.
     */
    protected final ObjectProvider<PasswordManagementService> passwordManagementService;

    /**
     * Build the reset URL for the user.
     */
    protected final ObjectProvider<PasswordResetUrlBuilder> passwordResetUrlBuilder;

    protected final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory;

    protected final ObjectProvider<ServicesManager> servicesManager;

    /**
     * The principal resolver to resolve the user
     * and fetch attributes for follow-up ops, such as email message body building.
     */
    protected final ObjectProvider<PrincipalResolver> principalResolver;

    protected final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    public PasswordManagementEndpoint(final CasConfigurationProperties casProperties,
                                      final ConfigurableApplicationContext applicationContext,
                                      final ObjectProvider<CommunicationsManager> communicationsManager,
                                      final ObjectProvider<PasswordManagementService> passwordManagementService,
                                      final ObjectProvider<PasswordResetUrlBuilder> passwordResetUrlBuilder,
                                      final ObjectProvider<ServiceFactory<WebApplicationService>> serviceFactory,
                                      final ObjectProvider<ServicesManager> servicesManager,
                                      final ObjectProvider<PrincipalResolver> principalResolver,
                                      final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
                                      final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer) {
        super(casProperties, applicationContext);
        this.communicationsManager = communicationsManager;
        this.passwordManagementService = passwordManagementService;
        this.passwordResetUrlBuilder = passwordResetUrlBuilder;
        this.serviceFactory = serviceFactory;
        this.servicesManager = servicesManager;
        this.principalResolver = principalResolver;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
    }

    /**
     * Password reset ops.
     *
     * @return the response entity
     */
    @Operation(summary = "Initiate a password reset operation and notify the user",
        parameters = {
            @Parameter(name = "username", description = "The username to reset the password for"),
            @Parameter(name = "service", description = "The service requesting the password reset")
        })
    @PostMapping(path = "/reset/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity passwordReset(@PathVariable("username") final String username,
                                        @RequestParam("service") final String service,
                                        final HttpServletRequest request) throws Throwable {
        val query = PasswordManagementQuery.builder().username(username).build();

        val email = passwordManagementService.getObject().findEmail(query);
        val phone = passwordManagementService.getObject().findPhone(query);
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) {
            val message = "No recipient is provided with a valid email/phone for %s".formatted(username);
            LOGGER.warn(message);
            return ResponseEntity.unprocessableEntity().body(message);
        }

        val webApplicationService = serviceFactory.getObject().createService(service);
        val registeredService = servicesManager.getObject().findServiceBy(webApplicationService);
        val principal = resolvedPrincipal(username);

        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .service(webApplicationService)
            .principal(principal)
            .httpRequest(request)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.getObject().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val url = passwordResetUrlBuilder.getObject().build(username, webApplicationService);
        val pm = casProperties.getAuthn().getPm();
        val duration = Beans.newDuration(pm.getReset().getExpiration());
        LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url, duration);
        val sendEmail = sendPasswordResetEmailToAccount(principal, email, url, request);
        val sendSms = sendPasswordResetSmsToAccount(phone, url);
        return sendEmail.isSuccess() || sendSms
            ? ResponseEntity.ok().build()
            : ResponseEntity.unprocessableEntity().body("Failed to send password reset instructions to %s".formatted(username));
    }

    protected boolean sendPasswordResetSmsToAccount(final String to, final URL url) throws Throwable {
        if (StringUtils.isNotBlank(to)) {
            LOGGER.debug("Sending password reset URL [{}] via SMS to [{}]", url.toExternalForm(), to);
            val reset = casProperties.getAuthn().getPm().getReset().getSms();
            val message = SmsBodyBuilder.builder().properties(reset).parameters(Map.of("url", url.toExternalForm())).build().get();
            val smsRequest = SmsRequest.builder()
                .from(reset.getFrom())
                .to(List.of(to))
                .text(message)
                .build();
            return communicationsManager.getObject().sms(smsRequest);
        }
        return false;
    }

    protected Principal resolvedPrincipal(final String username) throws Throwable {
        val resolvedPrincipal = principalResolver.getObject().resolve(new BasicIdentifiableCredential(username));
        return resolvedPrincipal instanceof NullPrincipal
            ? authenticationSystemSupport.getObject().getPrincipalFactory().createPrincipal(username)
            : resolvedPrincipal;
    }

    protected EmailCommunicationResult sendPasswordResetEmailToAccount(
        final Principal principal, final String to, final URL url,
        final HttpServletRequest request) {
        val reset = casProperties.getAuthn().getPm().getReset().getMail();
        val parameters = CollectionUtils.wrap("url", url.toExternalForm(), "principal", principal);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val text = EmailMessageBodyBuilder
            .builder()
            .properties(reset)
            .parameters(parameters)
            .locale(locale)
            .build()
            .get();
        LOGGER.debug("Sending password reset URL [{}] via email to [{}] for username [{}]",
            url, to, principal.getId());
        val emailRequest = EmailMessageRequest
            .builder()
            .emailProperties(reset)
            .principal(principal)
            .to(List.of(to))
            .locale(locale.orElseGet(Locale::getDefault))
            .body(text)
            .build();
        return communicationsManager.getObject().email(emailRequest);
    }
}
