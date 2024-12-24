package org.apereo.cas.web.flow;

import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CasFlowIdExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface CasFlowIdExtractor extends Ordered {
    /**
     * Extract flow id from request.
     *
     * @param request the request
     * @param flowId  the id
     * @return the flow id
     */
    default String extract(final HttpServletRequest request, final String flowId) {
        return flowId;
    }

    /**
     * Supports current flow id from the request?
     *
     * @param request the request
     * @param flowId  the flow id
     * @return true/false
     */
    default boolean supports(final HttpServletRequest request, final String flowId) {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * No op cas flow id extractor.
     *
     * @return the cas flow id extractor
     */
    static CasFlowIdExtractor noOp() {
        return new CasFlowIdExtractor() {
        };
    }
}
