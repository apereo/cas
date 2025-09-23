package org.apereo.cas.web.flow.action;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
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
public class SurrogateSelectionAction extends BaseCasWebflowAction {
    /**
     * Surrogate Target parameter name.
     */
    public static final String PARAMETER_NAME_SURROGATE_TARGET = "surrogateTarget";

    private final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    @Audit(action = AuditableActions.SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION,
        actionResolverName = AuditActionResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION_RESOURCE_RESOLVER)
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val resultMap = new HashMap<String, Object>();
        try {
            val credential = WebUtils.getCredential(requestContext);
            if (credential instanceof final MutableCredential mc) {
                val surrogateTarget = WebUtils.getRequestParameterOrAttribute(requestContext, PARAMETER_NAME_SURROGATE_TARGET)
                    .orElse(StringUtils.EMPTY);
                LOGGER.debug("Located surrogate target as [{}]", surrogateTarget);

                if (StringUtils.isNotBlank(surrogateTarget)) {
                    resultMap.put(PARAMETER_NAME_SURROGATE_TARGET, surrogateTarget);
                    val registeredService = WebUtils.getRegisteredService(requestContext);
                    val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
                    mc.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateTarget));
                    val result = surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(builder, mc, registeredService);
                    result.ifPresent(bldr -> WebUtils.putAuthenticationResultBuilder(bldr, requestContext));
                } else {
                    LOGGER.warn("No surrogate identifier was selected or provided");
                }
                resultMap.put("primary", credential.getId());
            } else {
                LOGGER.debug("Credential is not supported [{}]", credential);
            }
            return success(resultMap);
        } catch (final Throwable e) {
            WebUtils.addErrorMessageToContext(requestContext, "screen.surrogates.account.selection.error",
                "Unable to accept or authorize selection");
            LoggingUtils.error(LOGGER, e);
            return error(new RuntimeException(e));
        }
    }
}
