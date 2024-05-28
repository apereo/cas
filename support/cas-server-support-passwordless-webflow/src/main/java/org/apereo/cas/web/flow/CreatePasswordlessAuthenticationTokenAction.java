package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link CreatePasswordlessAuthenticationTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class CreatePasswordlessAuthenticationTokenAction extends BasePasswordlessCasWebflowAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    private final CommunicationsManager communicationsManager;

    public CreatePasswordlessAuthenticationTokenAction(final CasConfigurationProperties casProperties,
                                                       final PasswordlessTokenRepository passwordlessTokenRepository,
                                                       final CommunicationsManager communicationsManager) {
        super(casProperties);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
        this.communicationsManager = communicationsManager;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val passwordlessRequest = PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class);
        return createAndSendPasswordlessToken(requestContext, user, passwordlessRequest);
    }

    protected Event createAndSendPasswordlessToken(final RequestContext requestContext, final PasswordlessUserAccount user,
                                                   final PasswordlessAuthenticationRequest passwordlessRequest) {
        val token = passwordlessTokenRepository.createToken(user, passwordlessRequest);
        communicationsManager.validate();
        emailToken(requestContext, user, token);
        smsToken(requestContext, user, token);

        LOGGER.info("Storing passwordless token for [{}]", user.getUsername());
        passwordlessTokenRepository.deleteTokens(user.getUsername());
        passwordlessTokenRepository.saveToken(user, passwordlessRequest, token);
        return success(token);
    }

    protected void smsToken(final RequestContext requestContext,
                            final PasswordlessUserAccount user,
                            final PasswordlessAuthenticationToken token) {
        if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
            val passwordlessProperties = casProperties.getAuthn().getPasswordless();
            FunctionUtils.doUnchecked(u -> {
                val smsProperties = passwordlessProperties.getTokens().getSms();
                val text = SmsBodyBuilder.builder().properties(smsProperties)
                    .parameters(Map.of("token", token)).build().get();
                val smsRequest = SmsRequest
                    .builder()
                    .from(smsProperties.getFrom())
                    .to(List.of(user.getPhone()))
                    .text(text)
                    .build();
                communicationsManager.sms(smsRequest);
            });
        }
    }

    protected void emailToken(final RequestContext requestContext, final PasswordlessUserAccount user,
                              final PasswordlessAuthenticationToken token) {
        val passwordlessProperties = casProperties.getAuthn().getPasswordless();
        if (communicationsManager.isMailSenderDefined() && StringUtils.isNotBlank(user.getEmail())) {
            val mail = passwordlessProperties.getTokens().getMail();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
                .map(resolver -> resolver.resolveLocale(request));
            val body = EmailMessageBodyBuilder.builder()
                .properties(mail)
                .locale(locale)
                .parameters(Map.of("token", token.getToken()))
                .build()
                .get();
            val emailRequest = EmailMessageRequest.builder()
                .emailProperties(mail)
                .locale(locale.orElseGet(Locale::getDefault))
                .to(List.of(user.getEmail())).body(body).build();
            communicationsManager.email(emailRequest);
        }
    }
}
