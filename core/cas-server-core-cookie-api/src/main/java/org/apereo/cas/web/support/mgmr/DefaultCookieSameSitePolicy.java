package org.apereo.cas.web.support.mgmr;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.cookie.CookieSameSitePolicy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jooq.lambda.Unchecked;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultCookieSameSitePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class DefaultCookieSameSitePolicy implements CookieSameSitePolicy {
    /**
     * Policy instance.
     */
    public static final CookieSameSitePolicy INSTANCE = new DefaultCookieSameSitePolicy();

    @Override
    public Optional<String> build(final HttpServletRequest request, final HttpServletResponse response,
                                  final CookieGenerationContext cookieGenerationContext) {
        val sameSitePolicy = cookieGenerationContext.getSameSitePolicy();
        if (ResourceUtils.doesResourceExist(sameSitePolicy)) {
            return buildSameSitePolicyFromScript(request, response, cookieGenerationContext);
        }
        if (sameSitePolicy.contains(".")) {
            return Unchecked.supplier(() -> {
                val clazz = ClassUtils.getClass(CookieSameSitePolicy.class.getClassLoader(), sameSitePolicy);
                return (CookieSameSitePolicy) clazz.getDeclaredConstructor().newInstance();
            }).get().build(request, response, cookieGenerationContext);
        }
        val result = switch (sameSitePolicy.toLowerCase(Locale.ENGLISH).trim()) {
            case "strict" -> CookieSameSitePolicy.strict();
            case "lax" -> CookieSameSitePolicy.lax();
            case "off" -> CookieSameSitePolicy.off();
            default -> CookieSameSitePolicy.none();
        };
        return result.build(request, response, cookieGenerationContext);
    }

    protected Optional<String> buildSameSitePolicyFromScript(final HttpServletRequest request, final HttpServletResponse response,
                                                             final CookieGenerationContext cookieGenerationContext) {
        return FunctionUtils.doUnchecked(() -> {
            val sameSitePolicy = cookieGenerationContext.getSameSitePolicy();
            val resource = ResourceUtils.getResourceFrom(sameSitePolicy);
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            try (val groovyResource = scriptFactory.fromResource(resource)) {
                return Optional.ofNullable(groovyResource.execute(
                    new Object[]{request, response, cookieGenerationContext, LOGGER}, String.class));
            }
        });
    }
}
