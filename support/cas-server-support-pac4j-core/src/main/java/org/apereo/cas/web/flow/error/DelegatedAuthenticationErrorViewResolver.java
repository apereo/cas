package org.apereo.cas.web.flow.error;

import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationErrorViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DelegatedAuthenticationErrorViewResolver extends DefaultErrorViewResolver {
    private final DelegatedClientAuthenticationFailureEvaluator failureEvaluator;

    public DelegatedAuthenticationErrorViewResolver(final ApplicationContext applicationContext,
                                                    final WebProperties.Resources resources,
                                                    final DelegatedClientAuthenticationFailureEvaluator failureEvaluator) {
        super(applicationContext, resources);
        this.failureEvaluator = failureEvaluator;
    }

    @Override
    public ModelAndView resolveErrorView(final HttpServletRequest request,
                                         final HttpStatus status, final Map<String, Object> map) {

        val mv = failureEvaluator.evaluate(request, status.value());
        val exception = request.getAttribute("jakarta.servlet.error.exception");
        if (exception != null) {
            val cause = ((Throwable) exception).getCause();
            if (cause instanceof UnauthorizedServiceException) {
                val mvError = new ModelAndView(CasWebflowConstants.VIEW_ID_DELEGATED_AUTHN_ERROR_VIEW, HttpStatus.FORBIDDEN);
                LOGGER.warn("Delegated authentication failed with the following details [{}]; Routing over to [{}]", map, mvError.getViewName());
                return mvError;
            }
        }
        return mv.orElseGet(() -> super.resolveErrorView(request, status, map));
    }
}
