package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.event.PasswordChangeFailureEvent;
import org.apereo.cas.pm.event.PasswordChangeSuccessEvent;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class PasswordChangeAction extends BaseCasWebflowAction {

    private static final String PASSWORD_VALIDATION_FAILURE_CODE = "pm.validationFailure";

    private static final String DEFAULT_MESSAGE = "Could not update the account password";

    private final PasswordManagementService passwordManagementService;

    private final PasswordValidationService passwordValidationService;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final PrincipalResolver principalResolver;

    private final CommunicationsManager communicationsManager;

    private final CasConfigurationProperties casProperties;

    protected PasswordChangeRequest getPasswordChangeRequest(final RequestContext requestContext) {
        val bean = requestContext.getFlowScope().get(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);
        bean.setUsername(PasswordManagementWebflowUtils.getPasswordResetUsername(requestContext));
        return bean;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val clientInfo = ClientInfoHolder.getClientInfo();
        val bean = getPasswordChangeRequest(requestContext);

        try {
            if (CasWebflowConfigurer.FLOW_ID_LOGIN.equals(WebUtils.getActiveFlow(requestContext))) {
                LOGGER.debug("Attempting to validate current password for username [{}]", bean.getUsername());
                val credential = WebUtils.getCredential(requestContext, UsernamePasswordCredential.class);
                if (bean.getCurrentPassword() == null || bean.getCurrentPassword().length == 0 || !Arrays.equals(bean.getCurrentPassword(), credential.getPassword())) {
                    LOGGER.error("Current password is not correct or is undefined");
                    return getErrorEvent(requestContext, PASSWORD_VALIDATION_FAILURE_CODE, DEFAULT_MESSAGE);
                }
            }

            LOGGER.debug("Attempting to validate the password change bean for username [{}]", bean.getUsername());
            if (StringUtils.isBlank(bean.getUsername()) || !passwordValidationService.isValid(bean)) {
                LOGGER.error("Failed to validate the provided password");
                return getErrorEvent(requestContext, PASSWORD_VALIDATION_FAILURE_CODE, DEFAULT_MESSAGE);
            }
            if (passwordManagementService.change(bean)) {
                val credential = new UsernamePasswordCredential(bean.getUsername(), bean.toPassword());
                WebUtils.putCredential(requestContext, credential);
                LOGGER.info("Password successfully changed for [{}]", bean.getUsername());

                val query = PasswordManagementQuery.builder().username(bean.getUsername()).build();
                val email = locatePasswordResetRequestEmail(requestContext, query);
                if (StringUtils.isNotBlank(email)) {
                    val result = sendPasswordResetConfirmationEmailToAccount(bean.getUsername(), email, requestContext);
                    LOGGER.debug("Password reset confirmation email sent to [{}] with result [{}]", result.getTo(), result.isSuccess());
                }
                applicationContext.publishEvent(new PasswordChangeSuccessEvent(this, clientInfo, bean));
                return getSuccessEvent(requestContext, bean);
            }
        } catch (final InvalidPasswordException e) {
            applicationContext.publishEvent(new PasswordChangeFailureEvent(this, clientInfo, bean, e));
            return getErrorEvent(requestContext,
                PASSWORD_VALIDATION_FAILURE_CODE + StringUtils.defaultIfBlank(e.getCode(), StringUtils.EMPTY),
                StringUtils.defaultIfBlank(e.getValidationMessage(), DEFAULT_MESSAGE),
                e.getParams());
        } catch (final Throwable e) {
            applicationContext.publishEvent(new PasswordChangeFailureEvent(this, clientInfo, bean, e));
            LoggingUtils.error(LOGGER, e);
        }
        return getErrorEvent(requestContext, "pm.updateFailure", DEFAULT_MESSAGE);
    }

    protected Event getSuccessEvent(final RequestContext requestContext,
                                    final PasswordChangeRequest bean) {
        return eventFactory
            .event(this, CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS,
                new LocalAttributeMap<>("passwordChangeRequest", bean));
    }

    protected Event getErrorEvent(final RequestContext requestContext, final String code, final String message, final Object... params) {
        WebUtils.addErrorMessageToContext(requestContext, code, message, params);
        val viewStateId = requestContext.getCurrentTransition().getAttributes().get(CasWebflowConstants.ATTRIBUTE_CURRENT_EVENT_VIEW);
        Objects.requireNonNull(viewStateId, "Original view state id cannot be undefined");
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.ATTRIBUTE_CURRENT_EVENT_VIEW, viewStateId);
    }

    protected Principal resolvedPrincipal(final String username) throws Throwable {
        val resolvedPrincipal = principalResolver.resolve(new BasicIdentifiableCredential(username));
        return resolvedPrincipal instanceof NullPrincipal
            ? authenticationSystemSupport.getPrincipalFactory().createPrincipal(username)
            : resolvedPrincipal;
    }

    protected EmailCommunicationResult sendPasswordResetConfirmationEmailToAccount(
        final String username, final String to, final RequestContext requestContext) throws Throwable {
        val reset = casProperties.getAuthn().getPm().getReset().getConfirmationMail();
        val person = resolvedPrincipal(username);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val locale = Optional.ofNullable(RequestContextUtils.getLocaleResolver(request))
            .map(resolver -> resolver.resolveLocale(request));
        val text = EmailMessageBodyBuilder
            .builder()
            .properties(reset)
            .parameters(Map.of("principal", person))
            .locale(locale)
            .build()
            .get();
        LOGGER.debug("Sending password reset confirmation email to [{}] for username [{}]", to, username);
        val emailRequest = EmailMessageRequest
            .builder()
            .emailProperties(reset)
            .principal(person)
            .to(List.of(to))
            .locale(locale.orElseGet(Locale::getDefault))
            .body(text)
            .build();
        return communicationsManager.email(emailRequest);
    }

    protected String locatePasswordResetRequestEmail(final RequestContext requestContext, final PasswordManagementQuery query) throws Throwable {
        val emailAttributes = casProperties.getAuthn().getPm().getReset().getMail().getAttributeName();
        val principal = authenticationSystemSupport.getPrincipalResolver().resolve(new BasicIdentifiableCredential(query.getUsername()));
        return emailAttributes
            .stream()
            .map(attribute -> principal.getSingleValuedAttribute(attribute, String.class))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElseGet(Unchecked.supplier(() -> passwordManagementService.findEmail(query)));
    }
}
