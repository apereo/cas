package org.apereo.cas.web.support.gen;

import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;


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
@Setter
public class CookieRetrievingCookieGenerator extends CookieGenerator implements Serializable, CasCookieBuilder {
    private static final long serialVersionUID = -4926982428809856313L;

    /**
     * Responsible for manging and verifying the cookie value.
     **/
    private final CookieValueManager casCookieValueManager;

    private final CookieGenerationContext cookieGenerationContext;

    public CookieRetrievingCookieGenerator(final CookieGenerationContext context) {
        this(context, NoOpCookieValueManager.INSTANCE);
    }

    public CookieRetrievingCookieGenerator(final CookieGenerationContext context,
                                           final CookieValueManager casCookieValueManager) {
        super.setCookieName(context.getName());
        super.setCookiePath(context.getPath());
        super.setCookieMaxAge(context.getMaxAge());
        super.setCookieSecure(context.isSecure());
        super.setCookieHttpOnly(context.isHttpOnly());
        this.setCookieDomain(context.getDomain());

        this.cookieGenerationContext = context;
        this.casCookieValueManager = casCookieValueManager;
    }

    @Override
    public void addCookie(final RequestContext requestContext, final String cookieValue) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val theCookieValue = this.casCookieValueManager.buildCookieValue(cookieValue, request);
        if (isRememberMeAuthentication(requestContext)) {
            LOGGER.trace("Creating cookie [{}] for remember-me authentication", getCookieName());
            val cookie = createCookie(theCookieValue);

            cookie.setMaxAge(cookieGenerationContext.getRememberMeMaxAge());
            cookie.setSecure(isCookieSecure());
            cookie.setHttpOnly(isCookieHttpOnly());
            cookie.setComment("CAS Cookie w/ Remember-Me");

            response.addCookie(cookie);
        } else {
            LOGGER.trace("Creating cookie [{}]", getCookieName());
            super.addCookie(response, theCookieValue);
        }
    }

    @Override
    public void addCookie(final HttpServletRequest request, final HttpServletResponse response, final String cookieValue) {
        val theCookieValue = this.casCookieValueManager.buildCookieValue(cookieValue, request);
        LOGGER.trace("Creating cookie [{}]", getCookieName());
        super.addCookie(response, theCookieValue);
    }

    @Override
    public String retrieveCookieValue(final HttpServletRequest request) {
        try {
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
                .map(ck -> this.casCookieValueManager.obtainCookieValue(ck, request))
                .orElse(null);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void setCookieDomain(final String cookieDomain) {
        super.setCookieDomain(StringUtils.defaultIfEmpty(cookieDomain, null));
    }

    private static Boolean isRememberMeAuthentication(final RequestContext requestContext) {
        if (isRememberMeProvidedInRequest(requestContext)) {
            LOGGER.debug("This request is from a remember-me authentication event");
            return Boolean.TRUE;
        }
        if (isRememberMeRecordedInAuthentication(requestContext)) {
            LOGGER.debug("The recorded authentication is from a remember-me request");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    protected Cookie createCookie(final String cookieValue) {
        val c = super.createCookie(cookieValue);
        c.setComment("CAS Cookie");
        return c;
    }

    private static Boolean isRememberMeRecordedInAuthentication(final RequestContext requestContext) {
        LOGGER.debug("Request does not indicate a remember-me authentication event. Locating authentication object from the request context...");
        val auth = WebUtils.getAuthentication(requestContext);
        if (auth == null) {
            return Boolean.FALSE;
        }
        val attributes = auth.getAttributes();
        LOGGER.trace("Located authentication attributes [{}]", attributes);

        if (attributes.containsKey(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME)) {
            val rememberMeValue = attributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            LOGGER.debug("Located remember-me authentication attribute [{}]", rememberMeValue);
            return rememberMeValue.contains(Boolean.TRUE);
        }
        return Boolean.FALSE;
    }

    private static boolean isRememberMeProvidedInRequest(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val value = request.getParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME);
        LOGGER.trace("Locating request parameter [{}] with value [{}]", RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, value);
        return StringUtils.isNotBlank(value) && WebUtils.isRememberMeAuthenticationEnabled(requestContext);
    }
}
