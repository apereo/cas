package org.apereo.cas.services.web;

import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.context.WebEngineContext;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link CasThymeleafTemplatesDirector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class CasThymeleafTemplatesDirector {
    private final CasWebflowExecutionPlan webflowExecutionPlan;

    /**
     * Gets url external form.
     *
     * @param url the url
     * @return the url external form
     */
    public String getUrlExternalForm(final URL url) {
        return url.toExternalForm();
    }

    /**
     * Gets exception class simple name.
     *
     * @param ex the ex
     * @return the exception class simple name
     */
    public String getExceptionClassSimpleName(final Throwable ex) {
        return ex.getClass().getSimpleName();
    }

    /**
     * Is login form viewable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormViewable(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        val providers = webflowExecutionPlan.getWebflowLoginContextProviders();
        return requestContext != null
            && (WebUtils.isCasLoginFormViewable(requestContext) || providers.isEmpty()
            || providers.stream().noneMatch(provider -> provider.isLoginFormViewable(requestContext)));
    }

    /**
     * Is login form username editable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputVisible(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        val providers = webflowExecutionPlan.getWebflowLoginContextProviders();
        return requestContext != null
               && (WebUtils.isCasLoginFormViewable(requestContext) || providers.isEmpty()
                   || providers.stream().anyMatch(provider -> provider.isLoginFormUsernameInputVisible(requestContext)));
    }

    /**
     * Is login form username input disabled.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputDisabled(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        return requestContext == null || !WebUtils.isCasLoginFormViewable(requestContext)
               || WebUtils.isGraphicalUserAuthenticationEnabled(requestContext)
               || webflowExecutionPlan.getWebflowLoginContextProviders()
                   .stream()
                   .anyMatch(provider -> provider.isLoginFormUsernameInputDisabled(requestContext));
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

    /**
     * Format.
     *
     * @param dateTime the date time
     * @param pattern  the pattern
     * @return the string
     */
    public String format(final LocalDateTime dateTime, final String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

}
