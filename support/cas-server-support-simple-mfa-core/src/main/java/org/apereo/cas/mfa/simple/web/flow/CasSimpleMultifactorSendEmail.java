package org.apereo.cas.mfa.simple.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasSimpleMultifactorSendEmail}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
public class CasSimpleMultifactorSendEmail {
    private final CommunicationsManager communicationsManager;
    private final CasSimpleMultifactorAuthenticationProperties properties;
    private final TenantExtractor tenantExtractor;

    protected EmailCommunicationResults send(final Principal principal, final Ticket tokenTicket,
                                             final List<String> recipients, final RequestContext requestContext) {
        val results = FunctionUtils.doIf(communicationsManager.isMailSenderDefined(),
            () -> {
                val body = prepareEmailMessageBody(principal, tokenTicket, requestContext);
                return List.of(sendEmail(requestContext, body, principal, recipients));
            },
            () -> List.<EmailCommunicationResult>of(EmailCommunicationResult.builder().success(false).build())).get();
        return new EmailCommunicationResults(results);
    }


    protected EmailCommunicationResults send(final Principal principal, final Ticket tokenTicket,
                                             final RequestContext requestContext) {
        val results = FunctionUtils.doIf(communicationsManager.isMailSenderDefined(),
            () -> {
                val body = prepareEmailMessageBody(principal, tokenTicket, requestContext);
                return properties.getMail().getAttributeName()
                    .stream()
                    .map(attribute -> sendEmail(requestContext, body, principal, attribute))
                    .collect(Collectors.toList());
            },
            () -> List.<EmailCommunicationResult>of(EmailCommunicationResult.builder().success(false).build())).get();
        return new EmailCommunicationResults(results);
    }

    protected EmailCommunicationResult sendEmail(final RequestContext requestContext,
                                                 final EmailMessageBodyBuilder body,
                                                 final Principal principal,
                                                 final List<String> recipients) {
        val emailRequest = prepareEmailMessageRequest(requestContext, body, principal, StringUtils.EMPTY).withTo(recipients);
        return communicationsManager.email(emailRequest);
    }

    protected EmailCommunicationResult sendEmail(final RequestContext requestContext,
                                                 final EmailMessageBodyBuilder body,
                                                 final Principal principal,
                                                 final String attribute) {
        val emailRequest = prepareEmailMessageRequest(requestContext, body, principal, attribute);
        return communicationsManager.email(emailRequest);
    }

    protected List<String> getEmailMessageRecipients(final Principal principal, final RequestContext requestContext) {
        return properties.getMail().getAttributeName()
            .stream()
            .map(attribute -> EmailMessageRequest.builder()
                .emailProperties(properties.getMail())
                .principal(principal)
                .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
                .attribute(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute))
                .build()
                .getRecipients())
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    protected EmailMessageRequest prepareEmailMessageRequest(final RequestContext requestContext,
                                                             final EmailMessageBodyBuilder bodyBuilder,
                                                             final Principal principal,
                                                             final String attribute) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        return EmailMessageRequest.builder()
            .emailProperties(properties.getMail())
            .locale(locale.orElseGet(Locale::getDefault))
            .principal(principal)
            .attribute(SpringExpressionLanguageValueResolver.getInstance().resolve(attribute))
            .body(bodyBuilder.get())
            .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
            .context(bodyBuilder.getParameters())
            .build();
    }

    protected EmailMessageBodyBuilder prepareEmailMessageBody(final Principal principal,
                                                              final Ticket tokenTicket,
                                                              final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val parameters = buildEmailBodyParameters(principal, tokenTicket);
        return EmailMessageBodyBuilder.builder()
            .properties(properties.getMail())
            .locale(locale)
            .parameters(parameters)
            .build();
    }

    protected Map<String, Object> buildEmailBodyParameters(final Principal principal, final Ticket tokenTicket) {
        val parameters = CoreAuthenticationUtils.convertAttributeValuesToObjects(principal.getAttributes());
        val token = tokenTicket.getId();
        val tokenWithoutPrefix = token.substring(CasSimpleMultifactorAuthenticationTicket.PREFIX.length() + 1);
        parameters.put("token", token);
        parameters.put("tokenWithoutPrefix", tokenWithoutPrefix);
        return parameters;
    }

    public record EmailCommunicationResults(List<EmailCommunicationResult> results) {
        boolean isAnyEmailSent() {
            return results.stream().anyMatch(EmailCommunicationResult::isSuccess);
        }
    }
}
