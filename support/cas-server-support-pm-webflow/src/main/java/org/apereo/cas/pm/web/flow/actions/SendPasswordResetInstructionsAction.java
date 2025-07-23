package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SendPasswordResetInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SendPasswordResetInstructionsAction extends BaseCasWebflowAction {

    /**
     * Parameter name to look up the user.
     */
    public static final String REQUEST_PARAMETER_USERNAME = "username";

    /**
     * The CAS configuration properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The communication manager for SMS/emails.
     */
    protected final CommunicationsManager communicationsManager;

    /**
     * The password management service.
     */
    protected final PasswordManagementService passwordManagementService;

    /**
     * Ticket registry instance to hold onto the token.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * Ticket factory instance.
     */
    protected final TicketFactory ticketFactory;

    /**
     * The principal resolver to resolve the user
     * and fetch attributes for follow-up ops, such as email message body building.
     */
    protected final PrincipalResolver principalResolver;

    /**
     * Build the reset URL for the user.
     */
    protected final PasswordResetUrlBuilder passwordResetUrlBuilder;

    protected final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    protected final AuthenticationSystemSupport authenticationSystemSupport;

    protected final ServicesManager servicesManager;

    @Audit(action = AuditableActions.REQUEST_CHANGE_PASSWORD,
        principalResolverName = AuditPrincipalResolvers.REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER,
        actionResolverName = AuditActionResolvers.REQUEST_CHANGE_PASSWORD_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.REQUEST_CHANGE_PASSWORD_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined() && !communicationsManager.isSmsSenderDefined()) {
            return getErrorEvent("contact.failed", "Unable to send email as no mail sender is defined", requestContext);
        }

        val query = buildPasswordManagementQuery(requestContext);
        if (StringUtils.isBlank(query.getUsername())) {
            return getErrorEvent("username.required", "No username is provided", requestContext);
        }

        
        val email = locatePasswordResetRequestEmail(requestContext, query);
        val phone = locatePasswordResetRequestPhone(requestContext, query);
        
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) {
            LOGGER.warn("No recipient is provided with a valid email/phone");
            return getInvalidContactEvent(requestContext);
        }
        WebUtils.putPasswordManagementQuery(requestContext, query);
        if (doesPasswordResetRequireMultifactorAuthentication(requestContext)
            && !hasPrincipalRegisteredMultifactorAuthenticationDevice(requestContext)) {
            LOGGER.warn("No registered devices for multifactor authentication could be found for [{}]", query.getUsername());
            WebUtils.addErrorMessageToContext(requestContext, "screen.mfaDenied.message");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }
        val service = WebUtils.getService(requestContext);
        val url = buildPasswordResetUrl(query.getUsername(), service);
        if (url != null) {
            val sendEmail = sendPasswordResetEmailToAccount(query.getUsername(), email, url, requestContext);
            val sendSms = sendPasswordResetSmsToAccount(phone, url);
            if (sendEmail.isSuccess() || sendSms) {
                return success(url);
            }
        } else {
            LOGGER.error("No password reset URL could be built and sent to [{}]", email);
        }
        LOGGER.error("Failed to notify account [{}]", email);
        return getErrorEvent("contact.failed", "Failed to send the password reset link via email address or phone", requestContext);
    }

    protected String locatePasswordResetRequestPhone(final RequestContext requestContext, final PasswordManagementQuery query) throws Throwable {
        val phoneAttributes = casProperties.getAuthn().getPm().getReset().getSms().getAttributeName();
        val principal = authenticationSystemSupport.getPrincipalResolver().resolve(new BasicIdentifiableCredential(query.getUsername()));
        return phoneAttributes
            .stream()
            .map(attribute -> principal.getSingleValuedAttribute(attribute, String.class))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElseGet(Unchecked.supplier(() -> passwordManagementService.findPhone(query)));
    }

    protected String locatePasswordResetRequestEmail(final RequestContext requestContext, final PasswordManagementQuery query) throws Throwable {
        val emailAttributes = casProperties.getAuthn().getPm().getReset().getMail().getAttributeName();
        val principal = authenticationSystemSupport.getPrincipalResolver().resolve(new BasicIdentifiableCredential(query.getUsername()));
        return emailAttributes
            .stream()
            .map(attribute -> principal.getSingleValuedAttribute(attribute, String.class))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElseGet(Unchecked.supplier(() -> passwordManagementService.findEmail(query)));
    }

    protected boolean doesPasswordResetRequireMultifactorAuthentication(final RequestContext requestContext) throws Throwable {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val providerId = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(requestContext);
        var mfaRequired = casProperties.getAuthn().getPm().getReset().isMultifactorAuthenticationEnabled()
            && !providers.isEmpty() && StringUtils.isBlank(providerId);
        if (mfaRequired) {
            val query = WebUtils.getPasswordManagementQuery(requestContext, PasswordManagementQuery.class);
            val principal = resolvedPrincipal(query.getUsername());
            val provider = selectMultifactorAuthenticationProvider(requestContext, principal);
            val service = WebUtils.getService(requestContext);
            val registeredService = servicesManager.findServiceBy(service);
            if (service != null) {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            }

            val authentication = new DefaultAuthenticationBuilder(principal)
                .addCredential(new BasicIdentifiableCredential(query.getUsername()))
                .build();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val bypassEvaluator = provider.getBypassEvaluator();
            mfaRequired = bypassEvaluator == null || bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request, service);
        }
        return mfaRequired;
    }

    protected boolean hasPrincipalRegisteredMultifactorAuthenticationDevice(final RequestContext requestContext) throws Throwable {
        val query = WebUtils.getPasswordManagementQuery(requestContext, PasswordManagementQuery.class);
        val principal = resolvedPrincipal(query.getUsername());
        val provider = selectMultifactorAuthenticationProvider(requestContext, principal);
        LOGGER.debug("Selected multifactor authentication provider [{}]", provider.getId());
        return provider.getDeviceManager() == null || provider.getDeviceManager().hasRegisteredDevices(principal);
    }

    protected Principal resolvedPrincipal(final String username) throws Throwable {
        val resolvedPrincipal = principalResolver.resolve(new BasicIdentifiableCredential(username));
        return resolvedPrincipal instanceof NullPrincipal
            ? authenticationSystemSupport.getPrincipalFactory().createPrincipal(username)
            : resolvedPrincipal;
    }

    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(final RequestContext requestContext,
                                                                                        final Principal principal) throws Throwable {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        return multifactorAuthenticationProviderSelector.resolve(providers.values(), registeredService, principal);
    }

    protected PasswordManagementQuery buildPasswordManagementQuery(final RequestContext requestContext) {
        val existingQuery = WebUtils.getPasswordManagementQuery(requestContext, PasswordManagementQuery.class);
        return Optional.ofNullable(existingQuery)
            .orElseGet(() -> {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                val username = request.getParameter(REQUEST_PARAMETER_USERNAME);

                val builder = PasswordManagementQuery.builder();
                if (StringUtils.isBlank(username)) {
                    LOGGER.warn("No username parameter is provided");
                }
                return builder.username(username).build();
            });
    }

    protected Event getInvalidContactEvent(final RequestContext requestContext) {
        return getErrorEvent("contact.invalid", "Provided email address or phone number is invalid", requestContext);
    }

    protected boolean sendPasswordResetSmsToAccount(final String to, final URL url) throws Throwable {
        if (StringUtils.isNotBlank(to)) {
            LOGGER.debug("Sending password reset URL [{}] via SMS to [{}]", url.toExternalForm(), to);
            val reset = casProperties.getAuthn().getPm().getReset().getSms();
            val message = SmsBodyBuilder.builder().properties(reset).parameters(Map.of("url", url.toExternalForm())).build().get();
            val smsRequest = SmsRequest.builder().from(reset.getFrom()).to(List.of(to)).text(message).build();
            return communicationsManager.sms(smsRequest);
        }
        return false;
    }

    protected EmailCommunicationResult sendPasswordResetEmailToAccount(
        final String username, final String to, final URL url, final RequestContext requestContext) throws Throwable {
        val reset = casProperties.getAuthn().getPm().getReset().getMail();
        val parameters = CollectionUtils.<String, Object>wrap("url", url.toExternalForm());
        if (StringUtils.isNotBlank(to)) {
            val person = resolvedPrincipal(username);
            FunctionUtils.doIfNotNull(person, principal -> parameters.put("principal", principal));
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                .map(resolver -> resolver.resolveLocale(request));
            val text = EmailMessageBodyBuilder
                .builder()
                .properties(reset)
                .parameters(parameters)
                .locale(locale)
                .build()
                .get();
            LOGGER.debug("Sending password reset URL [{}] via email to [{}] for username [{}]", url, to, username);

            val emailRequest = EmailMessageRequest
                .builder()
                .emailProperties(reset)
                .principal(person)
                .to(List.of(to))
                .locale(locale.orElseGet(Locale::getDefault))
                .body(text)
                .build();
            return communicationsManager.email(emailRequest);
        }
        return EmailCommunicationResult.builder().success(false).to(List.of(to)).build();
    }

    protected Event getErrorEvent(final String code, final String defaultMessage,
                                  final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, "screen.pm.reset." + code, defaultMessage);
        LOGGER.error(defaultMessage);
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
    }

    protected URL buildPasswordResetUrl(final String username,
                                        final WebApplicationService service) throws Throwable {
        return passwordResetUrlBuilder.build(username, service);
    }
}
