package org.apereo.cas.services.web;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ThemeResolver;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link ThemeTemplateResolver}.
 *
 * @author Marcus Watkins
 * @since 6.0.0
 */
@Getter
@Slf4j
public class ThemeTemplateResolver extends SpringResourceTemplateResolver {
    /**
     * CAS settings.
     */
    protected final ThemeResolver themeResolver;
    
    public ThemeTemplateResolver(ThemeResolver themeResolver) {
    	this.themeResolver = themeResolver;
    	this.setOrder(0);
    	this.setCheckExistence(true);
    }

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration, final String ownerTemplate,
                                                        final String template, final String resourceName, final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        val theme = Optional.of(RequestContextHolder.currentRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(themeResolver::resolveThemeName);
        
        if (theme.isPresent()) {
        	val themeTemplate = String.format(resourceName, theme.get());
            ITemplateResource resource = super.computeTemplateResource(configuration, ownerTemplate, template, themeTemplate, characterEncoding, templateResolutionAttributes);
            if(resource.exists()) {
                LOGGER.debug("Successfully resolved themed template {}", themeTemplate);
            	return resource;
            }
            LOGGER.trace("No themed template available for {} using theme {}", resourceName, theme.get());
        }
        return null;
    }


}