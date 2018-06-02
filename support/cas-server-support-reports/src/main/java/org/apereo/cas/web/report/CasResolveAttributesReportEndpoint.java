package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasResolveAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Endpoint(id = "resolve-attributes", enableByDefault = false)
public class CasResolveAttributesReportEndpoint extends BaseCasMvcEndpoint {
    private final PrincipalResolver personDirectoryPrincipalResolver;

    public CasResolveAttributesReportEndpoint(final CasConfigurationProperties casProperties,
                                              final PrincipalResolver personDirectoryPrincipalResolver) {
        super(casProperties);
        this.personDirectoryPrincipalResolver = personDirectoryPrincipalResolver;
    }


    /**
     * Resolve principal attributes map.
     *
     * @param uid the uid
     * @return the map
     */
    @ReadOperation
    public Map<String, Object> resolvePrincipalAttributes(@Selector final String uid) {
        final var p = personDirectoryPrincipalResolver.resolve(new BasicIdentifiableCredential(uid));
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("uid", p.getId());
        map.put("attributes", p.getAttributes());
        return map;
    }
}
