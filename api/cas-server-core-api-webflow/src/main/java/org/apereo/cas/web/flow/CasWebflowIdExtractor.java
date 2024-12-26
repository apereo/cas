package org.apereo.cas.web.flow;

import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * This is {@link CasWebflowIdExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface CasWebflowIdExtractor extends Ordered {
    /**
     * Extract flow id from request.
     *
     * @param request the request
     * @param flowId  the id
     * @return the flow id
     */
    String extract(HttpServletRequest request, @NotNull @NotEmpty String flowId);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
