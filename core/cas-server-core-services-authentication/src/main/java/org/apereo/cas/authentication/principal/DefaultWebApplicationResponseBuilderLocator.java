package org.apereo.cas.authentication.principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This is {@link DefaultWebApplicationResponseBuilderLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultWebApplicationResponseBuilderLocator implements ResponseBuilderLocator<WebApplicationService> {
    private static final long serialVersionUID = 388417797622191740L;

    private final List<ResponseBuilder> builders;

    @Override
    public ResponseBuilder locate(final WebApplicationService service) {
        return builders.stream().filter(r -> r.supports(service)).findFirst().orElse(null);
    }
}
