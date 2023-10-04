package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.services.UnauthorizedServiceException;
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
 * This is {@link DisplayBeforePasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DisplayBeforePasswordlessAuthenticationAction extends BasePasswordlessCasWebflowAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    private final CommunicationsManager communicationsManager;

    private final PasswordlessRequestParser passwordlessRequestParser;

    public DisplayBeforePasswordlessAuthenticationAction(final CasConfigurationProperties casProperties,
                                                         final PasswordlessTokenRepository passwordlessTokenRepository,
                                                         final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                         final CommunicationsManager communicationsManager,
                                                         final PasswordlessRequestParser passwordlessRequestParser) {
        super(casProperties);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.communicationsManager = communicationsManager;
        this.passwordlessRequestParser = passwordlessRequestParser;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();
        if (attributes.contains(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            val error = attributes.get(CasWebflowConstants.TRANSITION_ID_ERROR, Exception.class);
            requestContext.getFlowScope().put(CasWebflowConstants.TRANSITION_ID_ERROR, error);
            val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            return success();
        }
        val username = requestContext.getRequestParameters().get(PasswordlessRequestParser.PARAMETER_USERNAME);
        if (StringUtils.isBlank(username)) {
            throw UnauthorizedServiceException.denied("Denied");
        }
        val passwordlessRequest = passwordlessRequestParser.parse(username);
        val account = FunctionUtils.doUnchecked(() -> passwordlessUserAccountStore.findUser(passwordlessRequest.getUsername()));
        if (account.isEmpty()) {
            LOGGER.error("Unable to locate passwordless user account for [{}]", username);
            throw UnauthorizedServiceException.denied("Denied: %s".formatted(username));
        }
        val user = account.get();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        val token = passwordlessTokenRepository.createToken(user, passwordlessRequest);

        communicationsManager.validate();
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
        if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
            FunctionUtils.doUnchecked(u -> {
                val smsProperties = passwordlessProperties.getTokens().getSms();
                val text = SmsBodyBuilder.builder().properties(smsProperties)
                    .parameters(Map.of("token", token)).build().get();
                val smsRequest = SmsRequest.builder().from(smsProperties.getFrom())
                    .to(user.getPhone()).text(text).build();
                communicationsManager.sms(smsRequest);
            });
        }
        LOGGER.info("Storing passwordless token for [{}]", user.getUsername());
        passwordlessTokenRepository.deleteTokens(user.getUsername());
        passwordlessTokenRepository.saveToken(user, passwordlessRequest, token);
        return success();
    }
}
