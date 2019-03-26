package org.apereo.cas.web.report;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasResolveAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Endpoint(id = "resolveAttributes", enableByDefault = false)
public class CasResolveAttributesReportEndpoint extends BaseCasActuatorEndpoint {
    private final PrincipalResolver defaultPrincipalResolver;

    public CasResolveAttributesReportEndpoint(final CasConfigurationProperties casProperties,
                                              final PrincipalResolver defaultPrincipalResolver) {
        super(casProperties);
        this.defaultPrincipalResolver = defaultPrincipalResolver;
    }


    /**
     * Resolve principal attributes map.
     *
     * @param uid the uid
     * @return the map
     */
    @ReadOperation
    public Map<String, Object> resolvePrincipalAttributes(@Selector final String uid) {
        val p = defaultPrincipalResolver.resolve(new BasicIdentifiableCredential(uid));
        val map = new HashMap<String, Object>();
        map.put("uid", p.getId());
        map.put("attributes", p.getAttributes());
        return map;
    }
}
