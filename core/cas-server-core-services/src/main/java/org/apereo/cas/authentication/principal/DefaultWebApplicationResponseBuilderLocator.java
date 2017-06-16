package org.apereo.cas.authentication.principal;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultWebApplicationResponseBuilderLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultWebApplicationResponseBuilderLocator implements ResponseBuilderLocator<WebApplicationService> {

    private static final long serialVersionUID = 388417797622191740L;
    private final transient List<ResponseBuilder> builders;

    public DefaultWebApplicationResponseBuilderLocator(final ApplicationContext applicationContext) {
        final Map<String, ResponseBuilder> beans = applicationContext.getBeansOfType(ResponseBuilder.class, false, true);
        this.builders = beans.values().stream().collect(Collectors.toList());
        AnnotationAwareOrderComparator.sortIfNecessary(builders);
    }

    @Override
    public ResponseBuilder locate(final WebApplicationService service) {
        return builders.stream().filter(r -> r.supports(service)).findFirst().orElse(null);
    }
}
