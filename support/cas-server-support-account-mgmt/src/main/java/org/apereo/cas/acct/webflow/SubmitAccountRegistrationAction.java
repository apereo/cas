package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link SubmitAccountRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class SubmitAccountRegistrationAction extends AbstractAction {
    private final AccountRegistrationService accountRegistrationService;

    private final CasConfigurationProperties casProperties;

    private final CommunicationsManager communicationsManager;

    private final TicketFactory ticketFactory;

    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val properties = accountRegistrationService.getAccountRegistrationPropertyLoader().load().values();
            val registrationRequest = new AccountRegistrationRequest();
            properties.forEach(entry -> {
                var value = entry.isRequired()
                    ? requestContext.getRequestParameters().getRequired(entry.getName())
                    : requestContext.getRequestParameters().get(entry.getName());
                registrationRequest.putProperty(entry.getName(), value);
            });

            val username = accountRegistrationService.getAccountRegistrationUsernameBuilder().build(registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequest(requestContext, registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequestUsername(requestContext, username);

            val url = createAccountRegistrationActivationUrl(registrationRequest);
            val sendEmail = sendAccountRegistrationActivationEmail(registrationRequest, url, requestContext);
            val sendSms = sendAccountRegistrationActivationSms(registrationRequest, url);
            if (sendEmail || sendSms) {
                return success(url);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, "cas.screen.acct.error.fail");
        return error();
    }

    /**
     * Send account registration activation sms.
     *
     * @param registrationRequest the registration request
     * @param url                 the url
     * @return the boolean
     */
    protected boolean sendAccountRegistrationActivationSms(final AccountRegistrationRequest registrationRequest, final String url) {
        if (StringUtils.isNotBlank(registrationRequest.getPhone())) {
            val smsProps = casProperties.getAccountRegistration().getSms();
            val message = smsProps.getFormattedText(url);
            return communicationsManager.sms(smsProps.getFrom(), registrationRequest.getPhone(), message);
        }
        return false;
    }


    /**
     * Send account registration activation email.
     *
     * @param registrationRequest the registration request
     * @param url                 the url
     * @param requestContext      the request context
     * @return the boolean
     */
    protected boolean sendAccountRegistrationActivationEmail(final AccountRegistrationRequest registrationRequest,
                                                             final String url,
                                                             final RequestContext requestContext) {
        if (StringUtils.isNotBlank(registrationRequest.getEmail())) {
            val emailProps = casProperties.getAccountRegistration().getMail();
            val parameters = CollectionUtils.<String, Object>wrap("url", url);
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val text = EmailMessageBodyBuilder.builder()
                .properties(emailProps)
                .parameters(parameters)
                .locale(Optional.ofNullable(request.getLocale()))
                .build()
                .produce();
            return communicationsManager.email(emailProps, registrationRequest.getEmail(), text);
        }
        return false;
    }

    /**
     * Create account registration activation url.
     *
     * @param registrationRequest the registration request
     * @return the string
     * @throws Exception the exception
     */
    protected String createAccountRegistrationActivationUrl(final AccountRegistrationRequest registrationRequest) throws Exception {
        val token = accountRegistrationService.createToken(registrationRequest);
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val properties = CollectionUtils.<String, Serializable>wrap(AccountRegistrationUtils.PROPERTY_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, token);
        val ticket = transientFactory.create((Service) null, properties);
        ticketRegistry.addTicket(ticket);
        return new URIBuilder(casProperties.getServer().getLoginUrl())
            .addParameter(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, ticket.getId())
            .build()
            .toURL()
            .toExternalForm();
    }
}
