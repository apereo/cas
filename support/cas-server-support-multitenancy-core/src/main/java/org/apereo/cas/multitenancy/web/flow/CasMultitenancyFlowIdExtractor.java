package org.apereo.cas.multitenancy.web.flow;

import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.flow.CasFlowIdExtractor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * This is {@link CasMultitenancyFlowIdExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor
public class CasMultitenancyFlowIdExtractor implements CasFlowIdExtractor {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    private static final Pattern PATTERN_TENANTS = RegexUtils.createPattern("tenants/(.+)/(.+)");

    private final TenantsManager tenantsManager;
    
    @Override
    public String extract(final HttpServletRequest request, final String flowId) {
        val matcher = PATTERN_TENANTS.matcher(flowId);
        if (matcher.find() && supports(request, flowId)) {
            val tenantId = matcher.group(1).trim();
            val tenantFlowId = matcher.group(2).trim();
            LOGGER.debug("Located tenant id [{}] for flow id [{}]", tenantId, tenantFlowId);
            return tenantFlowId;
        }
        return flowId;
    }

    @Override
    public boolean supports(final HttpServletRequest request, final String flowId) {
        val matcher = PATTERN_TENANTS.matcher(flowId);
        if (matcher.find()) {
            val tenantId = matcher.group(1).trim();
            return StringUtils.isNotBlank(tenantId) && tenantsManager.findTenant(tenantId).isPresent();
        }
        return false;
    }
}
