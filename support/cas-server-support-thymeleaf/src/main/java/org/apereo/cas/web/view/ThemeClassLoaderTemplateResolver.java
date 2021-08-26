package org.apereo.cas.web.view;

import org.apereo.cas.util.HttpRequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ThemeResolver;
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
    private final ThemeResolver themeResolver;

    @Override
    protected ITemplateResource computeTemplateResource(
        final IEngineConfiguration configuration,
        final String ownerTemplate, final String template,
        final String resourceName, final String characterEncoding,
        final Map<String, Object> templateResolutionAttributes) {

        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val themeName = this.themeResolver.resolveThemeName(request);

        if (StringUtils.isNotBlank(themeName)) {
            val themeTemplate = String.format(resourceName, themeName);
            val resource = new ClassLoaderTemplateResource(themeTemplate, StandardCharsets.UTF_8.name());
            if (resource.exists()) {
                LOGGER.trace("Computing template resource [{}]...", themeTemplate);
                return super.computeTemplateResource(configuration, ownerTemplate, template,
                    themeTemplate, characterEncoding, templateResolutionAttributes);
            }
        }
        return super.computeTemplateResource(configuration, ownerTemplate, template,
            resourceName, characterEncoding, templateResolutionAttributes);
    }
}

