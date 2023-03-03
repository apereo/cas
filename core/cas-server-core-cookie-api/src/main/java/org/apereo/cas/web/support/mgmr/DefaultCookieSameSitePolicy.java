package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieSameSitePolicy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jooq.lambda.Unchecked;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * This is {@link DefaultCookieSameSitePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCookieSameSitePolicy implements CookieSameSitePolicy {
    public static final CookieSameSitePolicy INSTANCE = new DefaultCookieSameSitePolicy();

    @Override
    public Optional<String> build(final HttpServletRequest request, final HttpServletResponse response,
                                  final CookieGenerationContext cookieGenerationContext) {
        val sameSitePolicy = cookieGenerationContext.getSameSitePolicy();
        if (sameSitePolicy.contains(".")) {
            return Unchecked.supplier(() -> {
                val clazz = ClassUtils.getClass(CookieSameSitePolicy.class.getClassLoader(), sameSitePolicy);
                return (CookieSameSitePolicy) clazz.getDeclaredConstructor().newInstance();
            }).get().build(request, response, cookieGenerationContext);
        }
        val result = switch (sameSitePolicy.toLowerCase().trim()) {
            case "strict" -> CookieSameSitePolicy.strict();
            case "lax" -> CookieSameSitePolicy.lax();
            case "off" -> CookieSameSitePolicy.off();
            default -> CookieSameSitePolicy.none();
        };
        return result.build(request, response, cookieGenerationContext);
    }
}
