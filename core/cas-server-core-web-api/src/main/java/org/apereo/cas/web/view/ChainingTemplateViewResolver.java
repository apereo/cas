package org.apereo.cas.web.view;

import lombok.Getter;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
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
    private List<AbstractTemplateResolver> resolvers = new ArrayList<>();

    public ChainingTemplateViewResolver() {
        setOrder(0);
        setCacheable(false);
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
        for (val r : this.resolvers) {
            val resource = r.resolveTemplate(configuration, ownerTemplate, template, templateResolutionAttributes);
            if (resource != null && resource.isTemplateResourceExistenceVerified()) {
                return resource.getTemplateResource();
            }
        }
        return null;
    }
}
