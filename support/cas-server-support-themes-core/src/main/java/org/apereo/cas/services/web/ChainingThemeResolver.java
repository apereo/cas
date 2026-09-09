package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.theme.AbstractThemeResolver;
import org.apereo.cas.web.theme.ThemeResolver;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * This is {@link ChainingThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class ChainingThemeResolver extends AbstractThemeResolver {

    private final Set<ThemeResolver> chain = new LinkedHashSet<>();

    /**
     * Theme names already matched to a theme definition. Only successful lookups are recorded,
     * so this is bounded by the themes the deployment actually ships.
     */
    private final Set<String> definedThemeNames = ConcurrentHashMap.newKeySet();

    private final ObjectProvider<CasConfigurationProperties> casProperties;

    /**
     * Add resolver to the chain.
     *
     * @param r the resolver
     * @return the chaining theme resolver
     */
    @CanIgnoreReturnValue
    public ChainingThemeResolver addResolver(final ThemeResolver r) {
        chain.add(r);
        return this;
    }

    @NonNull
    @Override
    public String resolveThemeName(
        final HttpServletRequest httpServletRequest) {
        for (val themeResolver : chain) {
            LOGGER.trace("Attempting to resolve theme via [{}]", themeResolver.getClass().getSimpleName());
            val resolverTheme = themeResolver.resolveThemeName(httpServletRequest);
            if (StringUtils.isNotBlank(resolverTheme) && !resolverTheme.equalsIgnoreCase(getDefaultThemeName())) {
                if (!isThemeDefined(resolverTheme)) {
                    LOGGER.debug("Theme [{}] resolved via [{}] does not match a theme definition and is ignored",
                        resolverTheme, themeResolver.getClass().getSimpleName());
                    continue;
                }
                LOGGER.trace("Resolved theme [{}]", resolverTheme);
                return resolverTheme;
            }
        }
        LOGGER.trace("No specific theme could be found. Using default theme [{}]", getDefaultThemeName());
        return getDefaultThemeName();
    }

    /**
     * Does the given name identify a theme this deployment defines? Every theme, however it is
     * chosen, is required to carry a {@code [theme].properties} file, either at the root of a
     * configured template prefix or at the root of the classpath.
     *
     * @param themeName the theme name
     * @return true/false
     */
    protected boolean isThemeDefined(final String themeName) {
        if (definedThemeNames.contains(themeName)) {
            return true;
        }
        val definition = "%s.properties".formatted(themeName);
        val defined = casProperties.getObject().getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> Strings.CI.appendIfMissing(prefix, "/").concat(definition))
            .anyMatch(ResourceUtils::doesResourceExist)
            || ResourceUtils.doesResourceExist(CLASSPATH_URL_PREFIX + definition);
        if (defined) {
            definedThemeNames.add(themeName);
        }
        return defined;
    }
}
