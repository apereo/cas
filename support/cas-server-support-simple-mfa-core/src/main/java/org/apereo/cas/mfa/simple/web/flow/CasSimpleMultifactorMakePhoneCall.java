package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.phone.PhoneProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.call.PhoneCallBodyBuilder;
import org.apereo.cas.notifications.call.PhoneCallRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorMakePhoneCall}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
public class CasSimpleMultifactorMakePhoneCall {
    private final CommunicationsManager communicationsManager;
    private final CasSimpleMultifactorAuthenticationProperties properties;

    protected boolean call(final Principal principal, final Ticket tokenTicket,
                           final RequestContext requestContext) {
        return FunctionUtils.doIf(communicationsManager.isPhoneOperatorDefined(),
            Unchecked.supplier(() -> {
                val phoneProperties = properties.getPhone();
                val token = tokenTicket.getId();
                val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
                val messageBody = buildMessageBody(phoneProperties, token, tokenWithoutPrefix);
                val callRequest = PhoneCallRequest.builder()
                    .from(phoneProperties.getFrom())
                    .principal(principal)
                    .attribute(phoneProperties.getAttributeName())
                    .text(messageBody)
                    .build();
                return communicationsManager.phoneCall(callRequest);
            }), () -> false).get();
    }

    protected String buildMessageBody(final PhoneProperties phoneProperties, final String token,
                                      final String tokenWithoutPrefix) {
        return FunctionUtils.doIfNotBlank(phoneProperties.getText(),
            () -> PhoneCallBodyBuilder.builder()
                .properties(phoneProperties)
                .parameters(Map.of("token", token, "tokenWithoutPrefix", tokenWithoutPrefix))
                .build()
                .get(),
            () -> token);
    }
}
