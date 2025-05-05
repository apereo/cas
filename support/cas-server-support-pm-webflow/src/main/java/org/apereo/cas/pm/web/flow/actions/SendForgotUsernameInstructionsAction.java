package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link SendForgotUsernameInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SendForgotUsernameInstructionsAction extends BaseCasWebflowAction {

    /**
     * Parameter name to look up the user.
     */
    public static final String REQUEST_PARAMETER_EMAIL = "email";

    protected final CasConfigurationProperties casProperties;

    protected final CommunicationsManager communicationsManager;

    protected final PasswordManagementService passwordManagementService;

    protected final PrincipalResolver principalResolver;

    protected final TenantExtractor tenantExtractor;

    @Audit(action = AuditableActions.REQUEST_FORGOT_USERNAME,
        principalResolverName = AuditPrincipalResolvers.REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER,
        actionResolverName = AuditActionResolvers.REQUEST_FORGOT_USERNAME_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.REQUEST_FORGOT_USERNAME_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        communicationsManager.validate();
        if (!communicationsManager.isMailSenderDefined()) {
            return getErrorEvent("email.failed", "Unable to send email as no mail sender is defined", requestContext);
        }
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val email = request.getParameter(REQUEST_PARAMETER_EMAIL);

        if (StringUtils.isBlank(email)) {
            return getErrorEvent("email.required", "No email is provided", requestContext);
        }

        if (!EmailValidator.getInstance().isValid(email)) {
            return getErrorEvent("email.invalid", "Provided email address is invalid", requestContext);
        }
        val query = PasswordManagementQuery.builder().email(email).build();
        val username = passwordManagementService.findUsername(query);
        if (StringUtils.isBlank(username)) {
            return getErrorEvent("username.missing", "No username could be located for the given email address", requestContext);
        }
        return locateUserAndProcess(requestContext, query.withUsername(username));
    }

    protected Event locateUserAndProcess(final RequestContext requestContext,
                                         final PasswordManagementQuery query) throws Throwable {
        val result = sendForgotUsernameEmailToAccount(query, requestContext);
        return FunctionUtils.doIf(result.isSuccess(),
                () -> success(result),
                () -> getErrorEvent("username.failed", "Cannot send the username to given email address", requestContext))
            .get();
    }

    protected EmailCommunicationResult sendForgotUsernameEmailToAccount(final PasswordManagementQuery query,
                                                                        final RequestContext requestContext) throws Throwable {
        val parameters = CollectionUtils.wrap("username", query.getUsername(), "email", query.getEmail());
        val credential = new BasicIdentifiableCredential();
        credential.setId(query.getUsername());
        val person = principalResolver.resolve(credential);
        FunctionUtils.doIf(person != null && !(person instanceof NullPrincipal),
            principal -> {
                parameters.put("principal", principal);
                requestContext.getFlashScope().put(Principal.class.getName(), person);
            }).accept(person);
        val reset = casProperties.getAuthn().getPm().getForgotUsername().getMail();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val body = EmailMessageBodyBuilder.builder()
            .properties(reset)
            .locale(locale)
            .parameters(parameters)
            .build()
            .get();
        val emailRequest = EmailMessageRequest.builder()
            .emailProperties(reset)
            .locale(locale.orElseGet(Locale::getDefault))
            .to(List.of(query.getEmail()))
            .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(StringUtils.EMPTY))
            .body(body)
            .build();
        return communicationsManager.email(emailRequest);
    }

    protected Event getErrorEvent(final String code, final String defaultMessage, final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, "screen.pm.forgotusername." + code, defaultMessage);
        LOGGER.error(defaultMessage);
        return eventFactory.event(this, CasWebflowConstants.VIEW_ID_ERROR);
    }
}
