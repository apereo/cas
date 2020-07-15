package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;

/**
 * This is {@link SurrogateSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateSelectionAction extends AbstractAction {
    /**
     * Surrogate Target parameter name.
     */
    public static final String PARAMETER_NAME_SURROGATE_TARGET = "surrogateTarget";

    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Audit(action = "SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION",
        actionResolverName = "SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_ACTION_RESOLVER",
        resourceResolverName = "SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_RESOURCE_RESOLVER")
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val resultMap = new HashMap<String, Object>();
        try {
            val credential = WebUtils.getCredential(requestContext);
            if (credential instanceof UsernamePasswordCredential) {
                val target = requestContext.getExternalContext().getRequestParameterMap().get(PARAMETER_NAME_SURROGATE_TARGET);
                LOGGER.debug("Located surrogate target as [{}]", target);
                if (StringUtils.isNotBlank(target)) {
                    val currentAuth = WebUtils.getAuthentication(requestContext);
                    AuthenticationCredentialsThreadLocalBinder.bindCurrent(currentAuth);
                    resultMap.put(PARAMETER_NAME_SURROGATE_TARGET, target);
                    val registeredService = WebUtils.getRegisteredService(requestContext);
                    val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
                    val result = surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(builder, credential, target, registeredService);
                    result.ifPresent(bldr -> WebUtils.putAuthenticationResultBuilder(bldr, requestContext));
                } else {
                    LOGGER.warn("No surrogate identifier was selected or provided");
                }
                resultMap.put("primary", credential.getId());
            } else {
                LOGGER.debug("Current credential in the webflow is not one of [{}]", UsernamePasswordCredential.class.getName());
            }
            return success(resultMap);
        } catch (final Exception e) {
            requestContext.getMessageContext().addMessage(new MessageBuilder()
                .error()
                .source("surrogate")
                .code("screen.surrogates.account.selection.error")
                .defaultText("Unable to accept or authorize selection")
                .build());
            LoggingUtils.error(LOGGER, e);
            return error(e);
        }
    }
}
