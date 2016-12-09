package org.apereo.cas.authentication.principal;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ResponseBuilder locate(final WebApplicationService service) {
        final Map<String, ResponseBuilder> beans = applicationContext.getBeansOfType(ResponseBuilder.class, false, true);
        final List<ResponseBuilder> builders = beans.values().stream().collect(Collectors.toList());
        AnnotationAwareOrderComparator.sortIfNecessary(builders);
        return builders.stream().filter(r -> r.supports(service)).findFirst().orElse(null);
    }
}
