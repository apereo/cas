package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link CasSimpleMultifactorSendSms}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
public class CasSimpleMultifactorSendSms {
    private final CommunicationsManager communicationsManager;
    private final CasSimpleMultifactorAuthenticationProperties properties;

    protected boolean send(final Principal principal, final Ticket tokenTicket,
                           final RequestContext requestContext, final List<String> recipients) {
        return FunctionUtils.doIf(communicationsManager.isSmsSenderDefined(),
            Unchecked.supplier(() -> {
                val smsProperties = properties.getSms();
                val token = tokenTicket.getId();
                val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
                val smsText = buildTextMessageBody(smsProperties, token, tokenWithoutPrefix);
                val smsRequest = SmsRequest.builder()
                    .from(smsProperties.getFrom())
                    .principal(principal)
                    .to(recipients)
                    .text(smsText)
                    .tenant(communicationsManager.getTenantExtractor()
                        .extract(requestContext).map(TenantDefinition::getId)
                        .orElse(StringUtils.EMPTY))
                    .build();
                return communicationsManager.sms(smsRequest);
            }), () -> false).get();
    }
    
    protected boolean send(final Principal principal, final Ticket tokenTicket,
                           final RequestContext requestContext) {
        return send(principal, tokenTicket, requestContext, getSmsRecipients(requestContext, principal));
    }

    protected List<String> getSmsRecipients(final RequestContext requestContext, final Principal principal) {
        val smsProperties = properties.getSms();
        return smsProperties.getAttributeName()
            .stream()
            .map(attribute -> SmsRequest.builder()
                .from(smsProperties.getFrom())
                .principal(principal)
                .tenant(communicationsManager.getTenantExtractor()
                    .extract(requestContext).map(TenantDefinition::getId)
                    .orElse(StringUtils.EMPTY))
                .attribute(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute))
                .build()
                .getRecipients())
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());
    }
    
    protected String buildTextMessageBody(final SmsProperties smsProperties, final String token,
                                          final String tokenWithoutPrefix) {
        return FunctionUtils.doIfNotBlank(smsProperties.getText(),
            () -> SmsBodyBuilder.builder()
                .properties(smsProperties)
                .parameters(Map.of("token", token, "tokenWithoutPrefix", tokenWithoutPrefix))
                .build()
                .get(),
            () -> token);
    }
}
