package org.apereo.cas.web.flow;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationErrorViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationErrorViewResolver implements ErrorViewResolver {

    private final ErrorViewResolver conventionErrorViewResolver;
    private final DelegatedSessionCookieManager delegatedSessionCookieManager;

    @Override
    public ModelAndView resolveErrorView(final HttpServletRequest request,
                                         final HttpStatus status, final Map<String, Object> map) {

        delegatedSessionCookieManager.removeCookie(Pac4jUtils.getPac4jJ2EContext(request));
        val mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, status.value());
        val exception = request.getAttribute("javax.servlet.error.exception");
        if (exception != null) {
            val cause = ((Throwable) exception).getCause();
            if (cause instanceof UnauthorizedServiceException) {
                val mvError = new ModelAndView("casDelegatedAuthnErrorView", HttpStatus.FORBIDDEN);
                LOGGER.warn("Delegated authentication has failed with the following details [{}]; Routing over to [{}]", map, mvError.getViewName());
                return mvError;
            }
        }
        return mv.orElseGet(() -> conventionErrorViewResolver.resolveErrorView(request, status, map));
    }
}
