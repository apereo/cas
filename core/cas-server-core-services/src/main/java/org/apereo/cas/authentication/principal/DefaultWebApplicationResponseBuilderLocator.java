package org.apereo.cas.authentication.principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

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
        return beans.values().stream().sorted().distinct().filter(r -> r.supports(service)).findFirst().orElse(null);
    }
}
