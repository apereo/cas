package org.apereo.cas.services.web;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ThemeResolver;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.Map;
import java.util.Optional;

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
    
    public ThemeTemplateResolver(final ThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
        this.setOrder(0);
        this.setCheckExistence(true);
    }

    @Override
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration, final String ownerTemplate,
                                                        final String template, final String resourceName, final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        val theme = Optional.of(RequestContextHolder.currentRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(themeResolver::resolveThemeName);
        
        if (theme.isPresent()) {
            LOGGER.debug("Attempting to resolve resource {} using theme {}", resourceName, theme.get());
            val themeTemplate = String.format(resourceName, theme.get());
            return super.computeTemplateResource(configuration, ownerTemplate, template, themeTemplate, characterEncoding, templateResolutionAttributes);
        }
        return null;
    }


}
