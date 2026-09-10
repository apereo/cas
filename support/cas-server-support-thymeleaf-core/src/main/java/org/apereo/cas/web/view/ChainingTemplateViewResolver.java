package org.apereo.cas.web.view;

import module java.base;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;

/**
 * This is {@link ChainingTemplateViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class ChainingTemplateViewResolver implements ITemplateResolver {
    private final List<ITemplateResolver> resolvers = new ArrayList<>();

    private final String name = getClass().getSimpleName();

    private final Integer order = 0;

    /**
     * Add resolver.
     *
     * @param resolver the resolver
     */
    public void addResolver(final ITemplateResolver resolver) {
        this.resolvers.add(resolver);
    }

    /**
     * Initialize and sort resolvers here before computing templates.
     */
    public void initialize() {
        AnnotationAwareOrderComparator.sortIfNecessary(this.resolvers);
    }

    @Override
    public @Nullable TemplateResolution resolveTemplate(final IEngineConfiguration configuration,
                                                        final String ownerTemplate,
                                                        final String template,
                                                        final Map<String, Object> templateResolutionAttributes) {
        return this.resolvers
            .stream()
            .map(resolver -> resolver.resolveTemplate(configuration, ownerTemplate, template, templateResolutionAttributes))
            .filter(resolution -> resolution != null && resolution.isTemplateResourceExistenceVerified())
            .findFirst()
            .orElse(null);
    }
}
