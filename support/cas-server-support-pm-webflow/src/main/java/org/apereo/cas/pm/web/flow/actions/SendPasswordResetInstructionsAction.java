package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
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
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URL;
import java.util.List;
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

    /**
     * Utility method to generate a password reset URL.
     *
     * @param username username
     * @param service  service from the flow scope
     * @return URL a user can use to start the password reset process
     * @throws Exception the exception
     */
    protected URL buildPasswordResetUrl(final String username,
                                        final WebApplicationService service) throws Exception {
        return passwordResetUrlBuilder.build(username, service);
    }

    @Audit(action = AuditableActions.REQUEST_CHANGE_PASSWORD,
        principalResolverName = "REQUEST_CHANGE_PASSWORD_PRINCIPAL_RESOLVER",
        actionResolverName = AuditActionResolvers.REQUEST_CHANGE_PASSWORD_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.REQUEST_CHANGE_PASSWORD_RESOURCE_RESOLVER)
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined() && !communicationsManager.isSmsSenderDefined()) {
            return getErrorEvent("contact.failed", "Unable to send email as no mail sender is defined", requestContext);
        }

        val query = buildPasswordManagementQuery(requestContext);
        if (StringUtils.isBlank(query.getUsername())) {
            return getErrorEvent("username.required", "No username is provided", requestContext);
        }

        val email = passwordManagementService.findEmail(query);
        val phone = passwordManagementService.findPhone(query);
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) {
            LOGGER.warn("No recipient is provided with a valid email/phone");
            return getErrorEvent("contact.invalid", "Provided email address or phone number is invalid", requestContext);
        }

        val service = WebUtils.getService(requestContext);
        val url = buildPasswordResetUrl(query.getUsername(), service);
        if (url != null) {
            val pm = casProperties.getAuthn().getPm();
            val duration = Beans.newDuration(pm.getReset().getExpiration());
            LOGGER.debug("Generated password reset URL [{}]; Link is only active for the next [{}] minute(s)", url, duration);
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

    protected PasswordManagementQuery buildPasswordManagementQuery(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = request.getParameter(REQUEST_PARAMETER_USERNAME);

        val builder = PasswordManagementQuery.builder();
        if (StringUtils.isBlank(username)) {
            LOGGER.warn("No username parameter is provided");
        }
        return builder.username(username).build();
    }

    protected boolean sendPasswordResetSmsToAccount(final String to, final URL url) {
        if (StringUtils.isNotBlank(to)) {
            LOGGER.debug("Sending password reset URL [{}] via SMS to [{}]", url.toExternalForm(), to);
            val reset = casProperties.getAuthn().getPm().getReset().getSms();
            val message = SmsBodyBuilder.builder().properties(reset).parameters(Map.of("url", url.toExternalForm())).build().get();
            val smsRequest = SmsRequest.builder().from(reset.getFrom()).to(to).text(message).build();
            return communicationsManager.sms(smsRequest);
        }
        return false;
    }

    protected EmailCommunicationResult sendPasswordResetEmailToAccount(
        final String username,
        final String to,
        final URL url,
        final RequestContext requestContext) {
        val reset = casProperties.getAuthn().getPm().getReset().getMail();
        val parameters = CollectionUtils.<String, Object>wrap("url", url.toExternalForm());
        if (StringUtils.isNotBlank(to)) {
            val credential = new BasicIdentifiableCredential();
            credential.setId(username);
            val person = principalResolver.resolve(credential);
            FunctionUtils.doIfNotNull(person, principal -> parameters.put("principal", principal));
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                .map(resolver -> resolver.resolveLocale(request));
            val text = EmailMessageBodyBuilder.builder()
                .properties(reset)
                .parameters(parameters)
                .locale(locale)
                .build()
                .get();
            LOGGER.debug("Sending password reset URL [{}] via email to [{}] for username [{}]", url, to, username);

            val emailRequest = EmailMessageRequest.builder().emailProperties(reset)
                .principal(person)
                .to(List.of(to)).body(text).build();
            return this.communicationsManager.email(emailRequest);
        }
        return EmailCommunicationResult.builder().success(false).to(List.of(to)).build();
    }

    /**
     * Gets error event.
     *
     * @param code           the code
     * @param defaultMessage the default message
     * @param requestContext the request context
     * @return the error event
     */
    protected Event getErrorEvent(final String code, final String defaultMessage,
                                  final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, "screen.pm.reset." + code, defaultMessage);
        LOGGER.error(defaultMessage);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
    }
}
