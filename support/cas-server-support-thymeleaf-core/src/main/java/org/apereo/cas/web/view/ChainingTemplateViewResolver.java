package org.apereo.cas.web.view;

import lombok.Getter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link ChainingTemplateViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class ChainingTemplateViewResolver extends AbstractConfigurableTemplateResolver {
    private final List<AbstractTemplateResolver> resolvers = new ArrayList<>(0);

    public ChainingTemplateViewResolver() {
        setOrder(0);
        setCacheable(false);
        setCheckExistence(true);
        setName(getClass().getSimpleName());
    }

    /**
     * Add resolver.
     *
     * @param resolver the resolver
     */
    public void addResolver(final AbstractTemplateResolver resolver) {
        this.resolvers.add(resolver);
    }

    /**
     * Initialize and sort resolvers here before computing templates.
     */
    public void initialize() {
        AnnotationAwareOrderComparator.sortIfNecessary(this.resolvers);
    }

    @Override
    protected ITemplateResource computeTemplateResource(final IEngineConfiguration configuration,
                                                        final String ownerTemplate,
                                                        final String template,
                                                        final String resourceName,
                                                        final String characterEncoding,
                                                        final Map<String, Object> templateResolutionAttributes) {
        return this.resolvers
            .stream()
            .map(r -> r.resolveTemplate(configuration, ownerTemplate, template, templateResolutionAttributes))
            .filter(resource -> resource != null && resource.isTemplateResourceExistenceVerified())
            .findFirst()
            .map(TemplateResolution::getTemplateResource)
            .orElse(null);
    }
}
