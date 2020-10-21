package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresource.ClassLoaderTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link ThemeClassLoaderTemplateResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class ThemeClassLoaderTemplateResolver extends ClassLoaderTemplateResolver {
    /**
     * CAS settings.
     */
    private final CasConfigurationProperties casProperties;

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration,
                                                        final String ownerTemplate, final String template,
                                                        final String resourceName, final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        val themeName = getCurrentTheme();
        if (StringUtils.isNotBlank(themeName)) {
            val themeTemplate = String.format(resourceName, themeName);
            val resource = new ClassLoaderTemplateResource(themeTemplate, StandardCharsets.UTF_8.name());
            if (resource.exists()) {
                LOGGER.trace("Computing template resource [{}]...", themeTemplate);
                return super.computeTemplateResource(configuration, ownerTemplate, template, themeTemplate, characterEncoding, templateResolutionAttributes);
            }
        }
        return super.computeTemplateResource(configuration, ownerTemplate, template, resourceName, characterEncoding, templateResolutionAttributes);
    }

    /**
     * Gets current theme.
     *
     * @return the current theme
     */
    protected String getCurrentTheme() {
        return getCurrentTheme(casProperties);
    }

    static String getCurrentTheme(final CasConfigurationProperties casProperties) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        String theme = null;
        if (request != null) {
            val session = request.getSession(false);
            val paramName = casProperties.getTheme().getParamName();
            if (session != null) {
                theme = (String) session.getAttribute(paramName);
                if (theme != null) {
                    return theme;
                }
            }
            theme = (String) request.getAttribute(paramName);
            if (theme != null) {
                return theme;
            }
        }

        return casProperties.getTheme().getDefaultThemeName();
    }
}

