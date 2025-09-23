package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SubmitAccountRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class SubmitAccountRegistrationAction extends BaseCasWebflowAction {
    private final AccountRegistrationService accountRegistrationService;

    private final CasConfigurationProperties casProperties;

    private final CommunicationsManager communicationsManager;

    private final TicketFactory ticketFactory;

    private final TicketRegistry ticketRegistry;

    private final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val registrationRequest = buildRegistrationRequest(requestContext);
            val username = accountRegistrationService.getAccountRegistrationUsernameBuilder().build(registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequest(requestContext, registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequestUsername(requestContext, username);

            val url = createAccountRegistrationActivationUrl(registrationRequest);
            val sendEmail = sendAccountRegistrationActivationEmail(registrationRequest, url, requestContext);
            val sendSms = sendAccountRegistrationActivationSms(requestContext, registrationRequest, url);
            if (sendEmail.isSuccess() || sendSms) {
                return success(url);
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, "cas.screen.acct.error.fail");
        return error();
    }

    protected AccountRegistrationRequest buildRegistrationRequest(final RequestContext requestContext) {
        val properties = accountRegistrationService.getAccountRegistrationPropertyLoader().load().values();
        val registrationRequest = new AccountRegistrationRequest();
        properties.forEach(entry -> {
            var value = entry.isRequired()
                ? requestContext.getRequestParameters().getRequired(entry.getName())
                : requestContext.getRequestParameters().get(entry.getName());
            registrationRequest.putProperty(entry.getName(), value);
        });
        accountRegistrationService.getAccountRegistrationRequestValidator().validate(registrationRequest);
        return registrationRequest;
    }

    protected boolean sendAccountRegistrationActivationSms(
        final RequestContext requestContext, final AccountRegistrationRequest registrationRequest,
        final String url) throws Throwable {
        if (StringUtils.isNotBlank(registrationRequest.getPhone())) {
            val smsProps = casProperties.getAccountRegistration().getSms();
            val message = SmsBodyBuilder.builder().properties(smsProps).parameters(Map.of("url", url)).build().get();
            val smsRequest = SmsRequest.builder()
                .from(smsProps.getFrom())
                .to(List.of(registrationRequest.getPhone()))
                .tenant(tenantExtractor.extract(requestContext)
                    .map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
                .text(message)
                .build();
            return communicationsManager.sms(smsRequest);
        }
        return false;
    }


    protected EmailCommunicationResult sendAccountRegistrationActivationEmail(final AccountRegistrationRequest registrationRequest,
                                                                              final String url,
                                                                              final RequestContext requestContext) {
        if (StringUtils.isNotBlank(registrationRequest.getEmail())) {
            val emailProps = casProperties.getAccountRegistration().getMail();
            val parameters = CollectionUtils.<String, Object>wrap("url", url);
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                .map(resolver -> resolver.resolveLocale(request));
            val text = EmailMessageBodyBuilder.builder()
                .properties(emailProps)
                .parameters(parameters)
                .locale(locale)
                .build()
                .get();
            val emailRequest = EmailMessageRequest.builder()
                .emailProperties(emailProps)
                .locale(locale.orElseGet(Locale::getDefault))
                .to(List.of(registrationRequest.getEmail()))
                .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
                .body(text).build();
            return communicationsManager.email(emailRequest);
        }
        return EmailCommunicationResult.builder().success(false).build();
    }

    protected String createAccountRegistrationActivationUrl(final AccountRegistrationRequest registrationRequest) throws Throwable {
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
