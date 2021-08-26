package org.apereo.cas.services.web;

import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.context.WebEngineContext;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link CasThymeleafLoginFormDirector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class CasThymeleafLoginFormDirector {
    private final CasWebflowExecutionPlan webflowExecutionPlan;

    /**
     * Is login form viewable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormViewable(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        return context != null
            && WebUtils.getDelegatedAuthenticationProviderPrimary(context) == null
            && WebUtils.isCasLoginFormViewable(context);
    }

    /**
     * Is login form username editable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputVisible(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        return context != null && WebUtils.isCasLoginFormViewable(context)
            && WebUtils.getPasswordlessAuthenticationAccount(context, Serializable.class) == null;
    }

    /**
     * Is login form username input disabled boolean.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputDisabled(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        return context == null || !WebUtils.isCasLoginFormViewable(context)
            || WebUtils.isGraphicalUserAuthenticationEnabled(context)
            || WebUtils.getPasswordlessAuthenticationAccount(context, Serializable.class) != null;
    }

    /**
     * Gets login form username.
     *
     * @param vars the vars
     * @return the login form username
     */
    public String getLoginFormUsername(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        if (context != null && WebUtils.isCasLoginFormViewable(context)) {
            return webflowExecutionPlan.getWebflowLoginContextProviders()
                .stream()
                .map(provider -> provider.getCandidateUsername(context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }
}
