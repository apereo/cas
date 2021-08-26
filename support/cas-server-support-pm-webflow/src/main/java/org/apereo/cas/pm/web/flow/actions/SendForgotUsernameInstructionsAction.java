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
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link SendForgotUsernameInstructionsAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SendForgotUsernameInstructionsAction extends AbstractAction {

    /**
     * Parameter name to look up the user.
     */
    public static final String REQUEST_PARAMETER_EMAIL = "email";

    /**
     * The CAS configuration properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The communication manager for SMS/emails.
     */
    protected final CommunicationsManager communicationsManager;

    /**
     * The password management service.
     */
    protected final PasswordManagementService passwordManagementService;

    /**
     * The principal resolver to resolve the user
     * and fetch attributes for follow-up ops, such as email message body building.
     */
    protected final PrincipalResolver principalResolver;

    @Audit(action = AuditableActions.REQUEST_FORGOT_USERNAME,
        principalResolverName = AuditPrincipalResolvers.REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER,
        actionResolverName = AuditActionResolvers.REQUEST_FORGOT_USERNAME_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.REQUEST_FORGOT_USERNAME_RESOURCE_RESOLVER)
    @Override
    protected Event doExecute(final RequestContext requestContext) {
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
        var query = PasswordManagementQuery.builder().email(email).build();
        val username = passwordManagementService.findUsername(query);
        if (StringUtils.isBlank(username)) {
            return getErrorEvent("username.missing", "No username could be located for the given email address", requestContext);
        }
        query = PasswordManagementQuery.builder().username(username).email(email).build();
        return locateUserAndProcess(requestContext, query);
    }

    /**
     * Process forgot username email and do a lookup.
     *
     * @param requestContext the request context
     * @param query          the query
     * @return the event
     */
    protected Event locateUserAndProcess(final RequestContext requestContext, final PasswordManagementQuery query) {
        if (sendForgotUsernameEmailToAccount(query, requestContext)) {
            return success();
        }
        return getErrorEvent("username.failed", "Failed to send the username to the given email address", requestContext);
    }

    /**
     * Send forgot username email to account.
     *
     * @param query          the query
     * @param requestContext the request context
     * @return the boolean
     */
    protected boolean sendForgotUsernameEmailToAccount(final PasswordManagementQuery query,
                                                       final RequestContext requestContext) {
        val parameters = CollectionUtils.<String, Object>wrap("email", query.getEmail());
        val credential = new BasicIdentifiableCredential();
        credential.setId(query.getUsername());
        val person = principalResolver.resolve(credential);
        FunctionUtils.doIf(person != null && !person.getClass().equals(NullPrincipal.class),
            principal -> {
                parameters.put("principal", principal);
                requestContext.getFlashScope().put(Principal.class.getName(), person);
            }).accept(person);
        val reset = casProperties.getAuthn().getPm().getForgotUsername().getMail();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val body = EmailMessageBodyBuilder.builder().properties(reset)
            .locale(Optional.ofNullable(request.getLocale()))
            .parameters(parameters).build().produce();
        return this.communicationsManager.email(reset, query.getEmail(), body);
    }

    /**
     * Locate and return the error event.
     *
     * @param code           the error code
     * @param defaultMessage the default message
     * @param requestContext the request context
     * @return the event
     */
    protected Event getErrorEvent(final String code, final String defaultMessage, final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, "screen.pm.forgotusername." + code, defaultMessage);
        LOGGER.error(defaultMessage);
        return new EventFactorySupport().event(this, CasWebflowConstants.VIEW_ID_ERROR);
    }
}
