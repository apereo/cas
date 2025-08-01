package org.apereo.cas.web.support.gen;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.InvalidCookieException;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Extends CookieGenerator to allow you to retrieve a value from a request.
 * The cookie is automatically marked as httpOnly, if the servlet container has support for it.
 * Also has support for remember-me.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class CookieRetrievingCookieGenerator implements Serializable, CasCookieBuilder {
    @Serial
    private static final long serialVersionUID = -4926982428809856313L;

    /**
     * Default path that cookies will be visible to: "/", i.e. the entire server.
     */
    private static final String DEFAULT_COOKIE_PATH = "/";

    private final CookieGenerationContext cookieGenerationContext;

    private final CookieValueManager casCookieValueManager;
    
    /**
     * Is remember me authentication ?
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static Boolean isRememberMeAuthentication(final RequestContext requestContext) {
        if (isRememberMeProvidedInRequest(requestContext)) {
            LOGGER.debug("This request is from a remember-me authentication event");
            return Boolean.TRUE;
        }
        val authn = WebUtils.getAuthentication(requestContext);
        if (CoreAuthenticationUtils.isRememberMeAuthentication(authn)) {
            LOGGER.debug("The recorded authentication is from a remember-me request");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static boolean isRememberMeProvidedInRequest(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val value = request.getParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME);
        LOGGER.trace("Locating request parameter [{}] with value [{}]", RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, value);
        return StringUtils.isNotBlank(value) && BooleanUtils.toBoolean(value) && WebUtils.isRememberMeAuthenticationEnabled(requestContext);
    }


    @Override
    public Cookie addCookie(final HttpServletRequest request, final HttpServletResponse response,
                            final boolean rememberMe, final String cookieValue) {
        val theCookieValue = casCookieValueManager.buildCookieValue(cookieValue, request);
        val cookie = createTenantCookie(createCookie(theCookieValue), request);

        if (rememberMe) {
            LOGGER.trace("Creating CAS cookie [{}] for remember-me authentication", getCookieName());
            cookie.setMaxAge(cookieGenerationContext.getRememberMeMaxAge());
        } else {
            LOGGER.trace("Creating CAS cookie [{}]", getCookieName());
            cookie.setMaxAge(cookieGenerationContext.getMaxAge());
        }
        cookie.setSecure(cookieGenerationContext.isSecure());
        cookie.setHttpOnly(cookieGenerationContext.isHttpOnly());
        return addCookieHeaderToResponse(cookie, request, response);
    }

    @Override
    public Cookie addCookie(final HttpServletRequest request, final HttpServletResponse response, final String cookieValue) {
        return addCookie(request, response, false, cookieValue);
    }

    @Override
    public String retrieveCookieValue(final HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(getCookieName())) {
                throw new InvalidCookieException("Cookie name is undefined");
            }
            var cookie = org.springframework.web.util.WebUtils.getCookie(request, Objects.requireNonNull(getCookieName()));
            if (cookie == null) {
                val cookieValue = request.getHeader(getCookieName());
                if (StringUtils.isNotBlank(cookieValue)) {
                    LOGGER.trace("Found cookie [{}] under header name [{}]", cookieValue, getCookieName());
                    cookie = createCookie(cookieValue);
                }
            }
            if (cookie == null) {
                val cookieValue = request.getParameter(getCookieName());
                if (StringUtils.isNotBlank(cookieValue)) {
                    LOGGER.trace("Found cookie [{}] under request parameter name [{}]", cookieValue, getCookieName());
                    cookie = createCookie(cookieValue);
                }
            }
            return Optional.ofNullable(cookie)
                .map(ck -> casCookieValueManager.obtainCookieValue(ck, request))
                .orElse(null);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }

    @Override
    public void removeCookie(final HttpServletResponse response) {
        val cookie = CookieUtils.createSetCookieHeader(null, cookieGenerationContext.withMaxAge(0));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        LOGGER.trace("Removed cookie [{}]", getCookieName());
    }

    @Override
    public String getCookiePath() {
        return cookieGenerationContext.getPath();
    }

    @Override
    public void setCookiePath(final String path) {
        cookieGenerationContext.setPath(cleanCookiePath(path));
    }

    @Override
    public String getCookieDomain() {
        return StringUtils.trimToNull(cookieGenerationContext.getDomain());
    }

    @Override
    public String getCookieName() {
        return cookieGenerationContext.getName();
    }

    @Override
    public void removeAll(final HttpServletRequest request, final HttpServletResponse response) {
        Optional.ofNullable(request.getCookies()).ifPresent(cookies -> Arrays.stream(cookies)
            .filter(cookie -> Strings.CI.equals(cookie.getName(), getCookieName()))
            .forEach(cookie ->
                Stream
                    .of("/", getCookiePath(),
                        Strings.CI.removeEnd(getCookiePath(), "/"),
                        Strings.CI.appendIfMissing(getCookiePath(), "/"))
                    .distinct()
                    .filter(StringUtils::isNotBlank)
                    .forEach(path -> {
                        val crm = new Cookie(cookie.getName(), cookie.getValue());
                        crm.setMaxAge(0);
                        crm.setPath(path);
                        crm.setSecure(cookie.getSecure());
                        crm.setHttpOnly(cookie.isHttpOnly());
                        LOGGER.debug("Removing cookie [{}] with path [{}] and [{}]", crm.getName(), crm.getPath(), crm.getValue());
                        response.addCookie(crm);
                    })));
    }

    @Override
    public boolean containsCookie(final HttpServletRequest request) {
        return request.getCookies() != null
            && Arrays.stream(request.getCookies()).anyMatch(cookie -> Strings.CI.equals(cookie.getName(), getCookieName()));
    }

    protected Cookie addCookieHeaderToResponse(final Cookie cookie,
                                               final HttpServletRequest request,
                                               final HttpServletResponse response) {
        val builder = new StringBuilder();
        builder.append(String.format("%s=%s;", cookie.getName(), cookie.getValue()));

        if (cookie.getMaxAge() > -1) {
            builder.append(String.format(" Max-Age=%s;", cookie.getMaxAge()));
        }
        if (StringUtils.isNotBlank(cookie.getDomain())) {
            builder.append(String.format(" Domain=%s;", cookie.getDomain()));
        }
        val path = cleanCookiePath(cookie.getPath());
        builder.append(String.format(" Path=%s;", path));

        val sameSitePolicy = casCookieValueManager.getCookieSameSitePolicy();
        val sameSiteResult = sameSitePolicy.build(request, response, cookieGenerationContext);
        sameSiteResult.ifPresent(result -> builder.append(String.format(" %s", result)));
        if (cookie.getSecure() || (sameSiteResult.isPresent() && Strings.CI.equals(sameSiteResult.get(), "none"))) {
            builder.append(" Secure;");
            LOGGER.trace("Marked cookie [{}] as secure as indicated by cookie configuration or the configured same-site policy", cookie.getName());
        }
        if (cookie.isHttpOnly()) {
            builder.append(" HttpOnly;");
        }
        val value = Strings.CI.removeEnd(builder.toString(), ";");
        LOGGER.trace("Adding cookie header as [{}]", value);
        val setCookieHeaders = response.getHeaders("Set-Cookie");
        response.setHeader("Set-Cookie", value);
        setCookieHeaders.stream()
            .filter(header -> !header.startsWith(cookie.getName() + '='))
            .forEach(header -> response.addHeader("Set-Cookie", header));
        return cookie;
    }

    private String cleanCookiePath(final String givenPath) {
        return FunctionUtils.doIf(StringUtils.isBlank(cookieGenerationContext.getPath()),
            () -> {
                val path = Strings.CI.removeEnd(StringUtils.defaultIfBlank(givenPath, DEFAULT_COOKIE_PATH), "/");
                return StringUtils.defaultIfBlank(path, "/");
            },
            () -> StringUtils.defaultIfBlank(givenPath, DEFAULT_COOKIE_PATH)).get();
    }

    private Cookie createTenantCookie(final Cookie cookie, final HttpServletRequest request) {
        val tenantDefinition = casCookieValueManager.getTenantExtractor().extract(request);
        tenantDefinition.ifPresent(tenant -> cookie.setPath(
            Strings.CI.appendIfMissing(cookie.getPath(), "/") + "tenants/" + tenant.getId()));
        return cookie;
    }

    protected Cookie createCookie(
        @NonNull
        final String cookieValue) {
        val cookie = new Cookie(getCookieName(), cookieValue);
        if (StringUtils.isNotBlank(getCookieDomain())) {
            cookie.setDomain(getCookieDomain());
        }
        cookie.setPath(cleanCookiePath(getCookiePath()));
        return cookie;
    }
}
