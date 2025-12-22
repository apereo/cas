package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
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
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    private final TenantExtractor tenantExtractor;

    public CreatePasswordlessAuthenticationTokenAction(final CasConfigurationProperties casProperties,
                                                       final PasswordlessTokenRepository passwordlessTokenRepository,
                                                       final CommunicationsManager communicationsManager,
                                                       final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                       final PrincipalFactory passwordlessPrincipalFactory,
                                                       final AuthenticationSystemSupport authenticationSystemSupport,
                                                       final TenantExtractor tenantExtractor) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
        this.communicationsManager = communicationsManager;
        this.tenantExtractor = tenantExtractor;
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val user = Objects.requireNonNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class));
        val passwordlessRequest = PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class);
        return createAndSendPasswordlessToken(requestContext, user, passwordlessRequest);
    }

    protected Event createAndSendPasswordlessToken(final RequestContext requestContext, final PasswordlessUserAccount user,
                                                   final PasswordlessAuthenticationRequest passwordlessRequest) {
        val token = passwordlessTokenRepository.createToken(user, passwordlessRequest);
        communicationsManager.validate();

        val emailSent = emailToken(requestContext, user, token);
        val smsSent = smsToken(requestContext, user, token);
        if (emailSent || smsSent) {
            LOGGER.info("Storing passwordless token for [{}]", user.getUsername());
            passwordlessTokenRepository.deleteTokens(user.getUsername());
            passwordlessTokenRepository.saveToken(user, passwordlessRequest, token);
            return success(token);
        }
        LOGGER.error("Failed to send passwordless token to [{}]", user.getUsername());
        return error();
    }

    protected boolean smsToken(final RequestContext requestContext,
                            final PasswordlessUserAccount user,
                            final PasswordlessAuthenticationToken token) {
        if (communicationsManager.isSmsSenderDefined() && StringUtils.isNotBlank(user.getPhone())) {
            val passwordlessProperties = casProperties.getAuthn().getPasswordless();
            FunctionUtils.doUnchecked(() -> {
                val smsProperties = passwordlessProperties.getTokens().getSms();
                val text = SmsBodyBuilder.builder()
                    .properties(smsProperties)
                    .parameters(Map.of("token", token.getToken()))
                    .build()
                    .get();
                val smsRequest = SmsRequest
                    .builder()
                    .from(smsProperties.getFrom())
                    .to(List.of(user.getPhone()))
                    .text(text)
                    .build();
                return communicationsManager.sms(smsRequest);
            });
        }
        return true;
    }

    protected boolean emailToken(final RequestContext requestContext, final PasswordlessUserAccount user,
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
                .to(List.of(user.getEmail()))
                .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
                .body(body)
                .build();
            return communicationsManager.email(emailRequest).isSuccess();
        }
        return true;
    }
}
