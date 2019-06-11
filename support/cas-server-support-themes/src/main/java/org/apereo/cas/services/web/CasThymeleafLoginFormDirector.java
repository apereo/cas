package org.apereo.cas.services.web;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.context.WebEngineContext;

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
     * @return the boolean
     */
    public boolean isLoginFormViewable(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        return context != null && WebUtils.getDelegatedAuthenticationProviderPrimary(context) == null
            && WebUtils.isCasLoginFormViewable(context);
    }
}
