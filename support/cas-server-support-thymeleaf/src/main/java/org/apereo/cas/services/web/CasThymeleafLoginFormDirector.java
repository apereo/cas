package org.apereo.cas.services.web;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.context.WebEngineContext;

import java.io.Serializable;

/**
 * This is {@link CasThymeleafLoginFormDirector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CasThymeleafLoginFormDirector {

    /**
     * Is login form viewable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormViewable(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        return context != null && WebUtils.getDelegatedAuthenticationProviderPrimary(context) == null
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
            && StringUtils.isBlank(WebUtils.getOpenIdLocalUserId(context))
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
            var user = WebUtils.getOpenIdLocalUserId(context);
            if (StringUtils.isBlank(user)) {
                val acct = WebUtils.getPasswordlessAuthenticationAccount(context, BasicIdentifiableCredential.class);
                if (acct != null) {
                    return acct.getId();
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
