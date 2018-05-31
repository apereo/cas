package org.apereo.cas.web.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Map;

/**
 * This is {@link ThemeFileTemplateResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class ThemeFileTemplateResolver extends FileTemplateResolver {
    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration, final String ownerTemplate,
                                                        final String template, final String resourceName, final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        final var themeName = getCurrentTheme();
        if (StringUtils.isNotBlank(themeName)) {
            final var themeTemplate = String.format(resourceName, themeName);
            return super.computeTemplateResource(configuration, ownerTemplate, template, themeTemplate, characterEncoding, templateResolutionAttributes);
        }
        return super.computeTemplateResource(configuration, ownerTemplate, template, resourceName, characterEncoding, templateResolutionAttributes);
    }

    /**
     * Gets current theme.
     *
     * @return the current theme
     */
    protected String getCurrentTheme() {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        if (request != null) {
            final var session = request.getSession(false);
            if (session != null) {
                return (String) session.getAttribute(casProperties.getTheme().getParamName());
            }
        }
        return null;
    }
}
