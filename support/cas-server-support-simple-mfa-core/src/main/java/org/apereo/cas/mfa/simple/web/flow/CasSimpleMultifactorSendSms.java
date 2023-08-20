package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsBodyBuilder;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorSendSms}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
class CasSimpleMultifactorSendSms {
    private final CommunicationsManager communicationsManager;
    private final CasSimpleMultifactorAuthenticationProperties properties;

    protected boolean send(final Principal principal, final Ticket tokenTicket,
                           final RequestContext requestContext) {
        return FunctionUtils.doIf(communicationsManager.isSmsSenderDefined(),
            Unchecked.supplier(() -> {
                val smsProperties = properties.getSms();
                val token = tokenTicket.getId();
                val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
                val smsText = buildTextMessageBody(smsProperties, token, tokenWithoutPrefix);
                val smsRequest = SmsRequest.builder()
                    .from(smsProperties.getFrom())
                    .principal(principal).attribute(smsProperties.getAttributeName())
                    .text(smsText)
                    .build();
                return communicationsManager.sms(smsRequest);
            }), () -> false).get();
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
