package org.apereo.cas.authentication.principal;

import module java.base;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultWebApplicationResponseBuilderLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DefaultWebApplicationResponseBuilderLocator implements ResponseBuilderLocator<WebApplicationService> {
    @Serial
    private static final long serialVersionUID = 388417797622191740L;

    private final List<ResponseBuilder> builders;

    @Override
    public @Nullable ResponseBuilder<WebApplicationService> locate(
        final WebApplicationService service) {
        return builders
            .stream()
            .filter(r -> r.supports(service))
            .findFirst()
            .orElse(null);
    }
}
