package org.apereo.cas.web.cookie;

import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jooq.lambda.Unchecked;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * This is {@link CookieSameSitePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface CookieSameSitePolicy {

    /**
     * Determine same-site policy based on given option.
     *
     * @param context the context
     * @return the cookie same site policy
     */
    static CookieSameSitePolicy of(final CookieGenerationContext context) {
        val option = context.getSameSitePolicy();
        if (option.contains(".")) {
            return Unchecked.supplier(() -> {
                val clazz = ClassUtils.getClass(CookieSameSitePolicy.class.getClassLoader(), option);
                return (CookieSameSitePolicy) clazz.getDeclaredConstructor().newInstance();
            }).get();
        }

        return switch (option.toLowerCase().trim()) {
            case "strict" -> strict();
            case "lax" -> lax();
            case "off" -> off();
            default -> none();
        };
    }

    /**
     * None cookie same site policy.
     *
     * @return the cookie same site policy
     */
    static CookieSameSitePolicy none() {
        return (request, response) -> Optional.of("SameSite=None;");
    }

    /**
     * Lax cookie same site policy.
     *
     * @return the cookie same site policy
     */
    static CookieSameSitePolicy lax() {
        return (request, response) -> Optional.of("SameSite=Lax;");
    }

    /**
     * Strict cookie same site policy.
     *
     * @return the cookie same site policy
     */
    static CookieSameSitePolicy strict() {
        return (request, response) -> Optional.of("SameSite=Strict;");
    }

    /**
     * Off cookie same site policy.
     *
     * @return the cookie same site policy
     */
    static CookieSameSitePolicy off() {
        return (request, response) -> Optional.empty();
    }

    /**
     * Build option string based on the same-site option type.
     *
     * @param request  the request
     * @param response the response
     * @return the string
     */
    Optional<String> build(HttpServletRequest request, HttpServletResponse response);
}
