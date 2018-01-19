package org.apereo.cas.web.view;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link ThemeFileTemplateResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@AllArgsConstructor
public class ThemeFileTemplateResolver extends FileTemplateResolver {
    private final CasConfigurationProperties casProperties;

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration, final String ownerTemplate,
                                                        final String template, final String resourceName, final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        if (request != null) {
            final String themeName = (String) request.getSession().getAttribute(casProperties.getTheme().getParamName());
            if (StringUtils.isNotBlank(themeName)) {
                final String themeTemplate = String.format(resourceName, themeName);
                return super.computeTemplateResource(configuration, ownerTemplate, template, themeTemplate,
                    characterEncoding, templateResolutionAttributes);
            }
        }
        return super.computeTemplateResource(configuration, ownerTemplate, template, resourceName,
            characterEncoding, templateResolutionAttributes);
    }
}
